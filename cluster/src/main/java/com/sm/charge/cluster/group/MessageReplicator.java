package com.sm.charge.cluster.group;

import com.sm.charge.cluster.BaseObject;
import com.sm.charge.cluster.Server;
import com.sm.charge.cluster.Store;
import com.sm.charge.cluster.exceptions.NotGroupServerException;
import com.sm.charge.cluster.messages.MessagePullRequest;
import com.sm.charge.cluster.messages.MessagePullResponse;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午10:31
 */
public class MessageReplicator extends LoggerSupport {

    private final Server self;
    private LeaderListener leaderListener;
    private final ServerGroup group;
    private final int maxBatchSize;
    private final Store store;
    private volatile Server leader;
    private final Map<Long, CompletableFuture> futureMap = new ConcurrentHashMap<>();

    public MessageReplicator(Server self, ServerGroup group, int maxBatchSize, Store store) {
        this.self = self;
        this.group = group;
        this.maxBatchSize = maxBatchSize;
        this.store = store;
    }


    public void pullMessage() {
        MessagePullRequest request = new MessagePullRequest(self.getAddress(), store.lastIndex());
        Connection connection = leader.getConnection();
        if (connection == null) {
            leaderListener.onLeave(leader);
            return;
        }

        connection.<MessagePullResponse>request(request).whenComplete((response, error) -> {
            if (error == null) {
                handle(response);
            } else {
                logger.error("pull message from leader:{} caught exception", leader, error);
                pullMessage();
            }
        });
    }


    public CompletableFuture<MessagePullResponse> handle(MessagePullRequest request) {
        Address address = request.getAddress();
        Server server = group.get(address);
        if (server == null) {
            logger.error("receive message pull request from:{},but isn't group member", request.getAddress());
            CompletableFuture<MessagePullResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new NotGroupServerException());
            return future;
        }

        return CompletableFuture.supplyAsync(() -> {
            long nextIndex = request.getNextIndex();
            server.setPullIndex(nextIndex - 1);
            updateWatermark();

            List<Entry> objects = new ArrayList<>(maxBatchSize);
            long lastIndex = store.lastIndex();
            int size = 0;
            for (; nextIndex <= lastIndex; nextIndex++) {
                BaseObject object = store.get(nextIndex);
                if (object == null) {
                    continue;
                }
                size++;
                if (size >= maxBatchSize) {
                    break;
                }

                objects.add(new Entry(nextIndex, object));
            }

            MessagePullResponse response = new MessagePullResponse();
            response.setWatermark(self.getWatermark());
            response.setEntries(objects);
            return response;
        });
    }

    private synchronized void updateWatermark() {
        List<Server> servers = group.getServers();
        servers.sort(Comparator.comparingLong(Server::getPullIndex));
        int quorum = group.getQuorum();
        if (servers.size() < quorum) {
            return;
        }

        Server server = servers.get(quorum - 1);
        long watermark = server.getPullIndex();

        Server leader = group.getLeader();
        long prevWatermark = leader.getWatermark();
        if (watermark > prevWatermark) {
            commit(watermark);
            while (++prevWatermark <= watermark) {
                CompletableFuture<?> future = futureMap.get(prevWatermark);
                if (future != null) {
                    future.complete(null);
                }
            }
        }
    }


    private void handle(MessagePullResponse response) {
        long watermark = response.getWatermark();
        if (watermark > self.getWatermark()) {
            commit(watermark);
        }

        List<Entry> entries = response.getEntries();
        long lastIndex = store.lastIndex();
        for (Entry entry : entries) {
            long skipSize = entry.getIndex() - lastIndex - 1;
            store.skip(skipSize);
            store.add(entry.getObject());
            lastIndex = entry.getIndex();
        }
    }

    private void commit(long watermark) {
        store.commit(Math.min(watermark, store.lastIndex()));
        self.setWatermark(watermark);
    }

    public void setLeader(Server leader) {
        this.leader = leader;
    }

    public void setLeaderListener(LeaderListener leaderListener) {
        this.leaderListener = leaderListener;
    }
}

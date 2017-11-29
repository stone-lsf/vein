package com.vein.cluster.group;

import com.vein.cluster.ClusterConfig;
import com.vein.cluster.ClusterMessage;
import com.vein.cluster.GroupLeaderListener;
import com.vein.cluster.Server;
import com.vein.cluster.Store;
import com.vein.common.AbstractService;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午10:39
 */
public class GroupServiceImpl extends AbstractService implements GroupService {

    private GroupMemberService membership;
    private MessagePullService replicator;
    private Store store;
    private ServerGroup group;

    public GroupServiceImpl(ServerGroup group, Server self, ClusterConfig config, Store store) {
        this.membership = new GroupMemberService(self, group, config.getElectTimeout(), store);
        this.replicator = new MessagePullService(self, group, config.getMaxAppendSize(), store);
        this.store = store;
        this.group = group;

        LeaderListener leaderListener = new GroupLeaderListener(replicator, membership);
        this.membership.setLeaderListener(leaderListener);
        this.replicator.setLeaderListener(leaderListener);
    }


    @Override
    public CompletableFuture<Boolean> receive(ClusterMessage message) {
        Entry entry = new Entry(message);
        long index = store.add(entry);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        group.addMessageFuture(index, future);
        return future;
    }

    @Override
    protected void doStart() throws Exception {
        membership.joinGroup();
    }

    @Override
    protected void doClose() throws Exception {
        replicator.close();
    }
}

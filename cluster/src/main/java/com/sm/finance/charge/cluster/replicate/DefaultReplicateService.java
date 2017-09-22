package com.sm.finance.charge.cluster.replicate;

import com.google.common.collect.Lists;

import com.sm.finance.charge.cluster.ClusterMember;
import com.sm.finance.charge.cluster.MemberState;
import com.sm.finance.charge.cluster.ServerContext;
import com.sm.finance.charge.cluster.discovery.DiscoveryNode;
import com.sm.finance.charge.cluster.storage.Log;
import com.sm.finance.charge.cluster.storage.entry.Entry;
import com.sm.finance.charge.cluster.storage.snapshot.Snapshot;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotManager;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.handler.AbstractResponseHandler;
import com.sm.finance.charge.transport.api.support.ResponseContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:36
 */
public class DefaultReplicateService extends AbstractService implements ReplicateService {

    private Log log;
    private ServerContext context;
    private final int maxBatchSize;
    private ExecutorService executorService;

    public DefaultReplicateService(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    protected void doStart() throws Exception {
        List<ClusterMember> members = context.getCluster().members();
        for (ClusterMember member : members) {
            executorService.execute(() -> replicate(member));
        }
    }

    @Override
    protected void doClose() {

    }

    public void replicate(ReplicateRequest data) {
        List<DiscoveryNode> candidates = Lists.newArrayList();

        MajorReplicateArbitrator arbitrator = new MajorReplicateArbitrator(candidates.size(), data);

        for (DiscoveryNode node : candidates) {
            Connection connection = node.getConnection();
            if (connection == null) {
                logger.error("replicate data to node:{} , but node don't have connection", node.getNodeId());
                arbitrator.flagOneFail();
                continue;
            }

            connection.send(data, new AbstractResponseHandler<ReplicateResponse>() {

                @Override
                public void onException(Exception e, ResponseContext context) {
                    logger.error("replicate data to node:{} caught exception:{}", node.getNodeId(), e);
                    arbitrator.flagOneFail();
                }

                @Override
                public void handle(ReplicateResponse response, Connection connection) {

                }
            });
        }
    }

    @Override
    public void replicate(ClusterMember member) {
        MemberState state = member.getState();
        long nextLogIndex = state.getNextLogIndex();
        if (needInstallSnapshot(nextLogIndex, state)) {
            InstallSnapshotRequest request = buildSnapshotRequest();
            this.replicateSnapshot(member, request).whenComplete((response, error) -> {
                if (error == null) {
                    this.handleInstallSnapshotResponse(response);
                } else {
                    this.handleInstallSnapshotResponseFailure(member, request, error);
                }
            });
            return;
        }

        Entry prevEntry = getPrevEntry(state);
        ReplicateRequest request = buildReplicateRequest(state, prevEntry);

        this.replicate(member, request).whenComplete((response, error) -> {
            if (error == null) {
                this.handleReplicateResponse(response);
            } else {
                this.handleReplicateResponseFailure(member, request, error);
            }
        });
    }

    private boolean needInstallSnapshot(long nextLogIndex, MemberState member) {
        SnapshotManager snapshotManager = context.getSnapshotManager();
        Snapshot snapshot = snapshotManager.currentSnapshot();

        return snapshot != null && snapshot.index() >= nextLogIndex && snapshot.index() > member.getSnapshotIndex();
    }

    private InstallSnapshotRequest buildSnapshotRequest() {

        return null;
    }

    private ReplicateRequest buildReplicateRequest(MemberState member, Entry prevEntry) {
        MemberState local = context.getMember().getState();

        ReplicateRequest request = new ReplicateRequest();
        request.setCurrentVersion(local.getVersion());

        request.setSource(local.getMember().getId());
        request.setDestination(member.getMember().getId());

        request.setPrevIndex(prevEntry == null ? 0 : prevEntry.getIndex());
        request.setPrevVersion(prevEntry == null ? 0 : prevEntry.getVersion());
        request.setCommitIndex(local.getCommittedIndex());

        Log log = context.getLog();
        long startIndex = (prevEntry == null ? log.firstIndex() : prevEntry.getIndex() + 1);
        long lastIndex = log.lastIndex();

        List<Entry> entries = new ArrayList<>(maxBatchSize);
        int size = 0;
        for (; startIndex <= lastIndex; startIndex++) {
            Entry entry = log.get(startIndex);
            if (entry != null) {
                if (!entries.isEmpty() && size + entry.getSize() > maxBatchSize) {
                    break;
                }
                size += entry.getSize();
                entries.add(entry);
            }
        }
        request.setEntries(entries);

        return request;
    }


    private Entry getPrevEntry(MemberState member) {
        long nextLogIndex = member.getNextLogIndex();
        if (nextLogIndex < 0) {
            return null;
        }

        long prevIndex = nextLogIndex - 1;
        Log log = context.getLog();
        long firstIndex = log.firstIndex();
        while (prevIndex >= firstIndex) {
            Entry entry = log.get(prevIndex);
            if (entry != null) {
                return entry;
            }
            prevIndex--;
        }
        return null;
    }

    private CompletableFuture<ReplicateResponse> replicate(ClusterMember member, ReplicateRequest request) {
        return null;
    }


    @Override
    public CompletableFuture<ReplicateResponse> handleReplicate(ReplicateRequest request) {
        return null;
    }

    @Override
    public void handleReplicateResponse(ReplicateResponse response) {

    }

    @Override
    public void handleReplicateResponseFailure(ClusterMember member, ReplicateRequest request, Throwable error) {

    }

    private CompletableFuture<InstallSnapshotResponse> replicateSnapshot(ClusterMember member, InstallSnapshotRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<InstallSnapshotResponse> install(InstallSnapshotRequest request) {
        return null;
    }

    @Override
    public void handleInstallSnapshotResponse(InstallSnapshotResponse request) {

    }

    @Override
    public void handleInstallSnapshotResponseFailure(ClusterMember member, InstallSnapshotRequest request, Throwable error) {

    }
}

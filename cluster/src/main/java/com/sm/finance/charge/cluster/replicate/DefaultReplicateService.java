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
import java.util.Comparator;
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
    private final int replicateTimeout;
    private final ClusterMember self;


    public DefaultReplicateService(int maxBatchSize, int replicateTimeout, ClusterMember self) {
        this.maxBatchSize = maxBatchSize;
        this.replicateTimeout = replicateTimeout;
        this.self = self;
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
                public void onException(Throwable e, ResponseContext context) {
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
                this.handleReplicateResponse(response, member);
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
            if (entry == null) {
                continue;
            }

            if (!entries.isEmpty() && size + entry.getSize() > maxBatchSize) {
                break;
            }
            size += entry.getSize();
            entries.add(entry);
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
        CompletableFuture<ReplicateResponse> result = new CompletableFuture<>();

        member.getConnection().whenComplete((connection, error) -> {
            if (error == null) {
                connection.<ReplicateResponse>request(request, replicateTimeout).whenComplete((response, e) -> {
                    if (e != null) {
                        result.completeExceptionally(e);
                    } else {
                        result.complete(response);
                    }
                });
            } else {
                result.completeExceptionally(error);
            }
        });

        return result;
    }


    @Override
    public CompletableFuture<ReplicateResponse> handleReplicate(ReplicateRequest request) {
        long requestVersion = request.getCurrentVersion();
        MemberState selfState = self.getState();
        long version = selfState.getVersion();

        ReplicateResponse response = new ReplicateResponse();
        response.setSuccess(false);
        if (requestVersion < version) {
            response.setVersion(version);
            return CompletableFuture.completedFuture(response);
        }

        long prevIndex = request.getPrevIndex();
        if (prevIndex > 0) {
            Entry prevEntry = log.get(prevIndex);
            if (prevEntry == null) {
                logger.warn("received replicate data from[{}], but prev log index don't match", request.getSource(), prevIndex);
                Entry lastEntry = log.lastEntry();
                response.setNextIndex(lastEntry == null ? 1 : lastEntry.getIndex() + 1);
                return CompletableFuture.completedFuture(response);
            }

            long localPrevVersion = prevEntry.getVersion();
            if (prevEntry.getVersion() != request.getPrevVersion()) {
                logger.warn("received replicate data from[{}], but local prev version:{} don't match request prev index:{}", request.getSource(), localPrevVersion, request.getPrevVersion());
                response.setNextIndex(prevIndex);
                return CompletableFuture.completedFuture(response);
            }
        }

        log.truncate(prevIndex);
        for (Entry entry : request.getEntries()) {
            log.append(entry);
        }

        commitTo(request.getCommitIndex());

        response.setSuccess(true);
        response.setNextIndex(log.lastIndex() + 1);
        return CompletableFuture.completedFuture(response);
    }

    private void commitTo(long index) {
        //TODO commit 日志
    }

    @Override
    public void handleReplicateResponse(ReplicateResponse response, ClusterMember member) {
        if (response.isSuccess()) {
            member.getState().setMatchedIndex(response.getNextIndex() - 1);
            commitEntries();
            if (!closed.get()) {
                executorService.execute(() -> replicate(member));
            }
            return;
        }

        if (response.getVersion() > self.getState().getVersion()) {
            try {
                this.close();
            } catch (Exception e) {
                logger.error("close replicate service caught exception", e);
            }

            //TODO 重新跟master获取联系

            return;
        }

        MemberState state = member.getState();
        state.setNextLogIndex(response.getNextIndex());
        if (!closed.get()) {
            executorService.execute(() -> replicate(member));
        }
    }

    private void commitEntries() {
        List<ClusterMember> members = context.getCluster().members();
        members.sort(Comparator.comparingLong(member -> member.getState().getMatchedIndex()));
        int quorum = context.getCluster().getQuorum();
        if (members.size() < quorum) {
            return;
        }
        ClusterMember member = members.get(quorum - 1);
        long commitIndex = member.getState().getMatchedIndex();

        MemberState selfState = self.getState();
        long prevCommittedIndex = selfState.getCommittedIndex();
        if (commitIndex > prevCommittedIndex) {
            selfState.setCommittedIndex(commitIndex);
            commitTo(commitIndex);
        }
    }

    @Override
    public void handleReplicateResponseFailure(ClusterMember member, ReplicateRequest request, Throwable error) {
        MemberState state = member.getState();
        state.incrementReplicateFailureCount();
        logger.error("replicate data to:{} failed:{}", member.getAddress(), error);
        if (!closed.get()) {
            executorService.execute(() -> replicate(member));
        }
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

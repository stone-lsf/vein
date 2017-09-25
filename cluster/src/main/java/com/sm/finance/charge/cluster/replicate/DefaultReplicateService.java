package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.cluster.ClusterMember;
import com.sm.finance.charge.cluster.MemberState;
import com.sm.finance.charge.cluster.ServerContext;
import com.sm.finance.charge.cluster.StateMachine;
import com.sm.finance.charge.cluster.storage.Log;
import com.sm.finance.charge.cluster.storage.entry.Entry;
import com.sm.finance.charge.cluster.storage.snapshot.Snapshot;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotManager;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotReader;
import com.sm.finance.charge.common.AbstractService;

import java.io.IOException;
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
    private final int snapshotTimeout;
    private final ClusterMember self;
    private final StateMachine stateMachine;


    public DefaultReplicateService(int maxBatchSize, int replicateTimeout, int snapshotTimeout, ClusterMember self, StateMachine stateMachine) {
        this.maxBatchSize = maxBatchSize;
        this.replicateTimeout = replicateTimeout;
        this.snapshotTimeout = snapshotTimeout;
        this.self = self;
        this.stateMachine = stateMachine;
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

    @Override
    public void replicate(ClusterMember member) {
        MemberState state = member.getState();
        long nextLogIndex = state.getNextLogIndex();
        if (needInstallSnapshot(nextLogIndex, state)) {
            this.snapshot(member);
            return;
        }

        Entry prevEntry = getPrevEntry(state);
        ReplicateRequest request = buildReplicateRequest(state, prevEntry);

        this.<ReplicateResponse>request(member, request, replicateTimeout).whenComplete((response, error) -> {
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

        apply(request.getCommitIndex());
        selfState.setCommittedIndex(request.getCommitIndex());

        response.setSuccess(true);
        response.setNextIndex(log.lastIndex() + 1);
        return CompletableFuture.completedFuture(response);
    }

    private void apply(long index) {
        MemberState state = self.getState();

        long lastApplied = state.getLastApplied();
        if (index < lastApplied + 1) {
            return;
        }

        for (long i = lastApplied + 1; i <= index; i++) {
            Entry entry = log.get(i);
            if (entry != null) {
                stateMachine.apply(entry).whenComplete((result, error) -> {
                    CompletableFuture<Object> future = self.romoveCommitFuture(entry.getIndex());
                    if (future != null) {
                        if (error == null) {
                            future.complete(result);
                        } else {
                            future.completeExceptionally(error);
                        }
                    }
                });
            }
        }
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
            apply(commitIndex);
            selfState.setCommittedIndex(commitIndex);
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

    @Override
    public void snapshot(ClusterMember member) {
        InstallSnapshotRequest request;
        try {
            request = buildSnapshotRequest(member);
        } catch (IOException e) {
            logger.warn("build snapshot request for member:{} failed", member.getId());
            executorService.execute(() -> replicate(member));
            return;
        }

        InstallSnapshotRequest finalRequest = request;
        this.<InstallSnapshotResponse>request(member, request, snapshotTimeout).whenComplete((response, error) -> {
            if (error == null) {
                this.handleInstallSnapshotResponse(response);
            } else {
                this.handleInstallSnapshotResponseFailure(member, finalRequest, error);
            }
        });
    }

    private <T> CompletableFuture<T> request(ClusterMember member, Object request, int timeout) {
        CompletableFuture<T> future = new CompletableFuture<>();
        member.getConnection().whenComplete((connection, error) -> {
            if (error == null) {
                connection.<T>request(request, timeout).whenComplete((response, e) -> {
                    if (e != null) {
                        future.completeExceptionally(e);
                    } else {
                        future.complete(response);
                    }
                });
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    private InstallSnapshotRequest buildSnapshotRequest(ClusterMember member) throws IOException {
        Snapshot snapshot = context.getSnapshotManager().currentSnapshot();

        MemberState state = member.getState();
        if (snapshot.index() != state.getNextSnapshotIndex()) {
            state.setNextSnapshotIndex(snapshot.index());
            state.setNextSnapshotOffset(0L);
        }

        InstallSnapshotRequest request = new InstallSnapshotRequest();
        request.setVersion(self.getState().getVersion());
        request.setFrom(self.getId());
        request.setIndex(state.getNextSnapshotIndex());
        request.setOffset(state.getNextSnapshotOffset());

        try (SnapshotReader reader = snapshot.reader()) {
            byte[] data = new byte[Math.min(maxBatchSize, (int) reader.remaining())];
            reader.read(data);
            request.setData(data);
            request.setComplete(!reader.hasRemaining());
        } catch (Exception e) {
            logger.error("read snapshot:{} data caught exception:{}", snapshot.index(), e);
            throw new IOException(e);
        }

        return request;
    }

    @Override
    public CompletableFuture<InstallSnapshotResponse> handleSnapshot(InstallSnapshotRequest request) {
        return null;
    }

    @Override
    public void handleInstallSnapshotResponse(InstallSnapshotResponse request) {

    }

    @Override
    public void handleInstallSnapshotResponseFailure(ClusterMember member, InstallSnapshotRequest request, Throwable error) {

    }
}

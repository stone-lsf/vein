package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.cluster.ClusterMember;
import com.sm.finance.charge.cluster.ClusterMemberState;
import com.sm.finance.charge.cluster.ServerContext;
import com.sm.finance.charge.cluster.ServerStateMachine;
import com.sm.finance.charge.cluster.storage.Entry;
import com.sm.finance.charge.cluster.storage.Log;
import com.sm.finance.charge.cluster.storage.snapshot.Snapshot;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotManager;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotReader;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotWriter;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.utils.IoUtil;
import com.sm.finance.charge.common.NamedThreadFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sm.finance.charge.common.SystemConstants.PROCESSORS;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:36
 */
public class DefaultReplicateService extends AbstractService implements ReplicateService {

    private final Log log;
    private final ServerContext context;
    private final int maxBatchSize;
    private final int replicateTimeout;
    private final int snapshotTimeout;
    private final ClusterMember self;
    private final ServerStateMachine stateMachine;
    private final ExecutorService executorService;
    private volatile Snapshot pendingSnapshot;
    private volatile long nextSnapshotOffset;

    public DefaultReplicateService(ServerContext context) {
        this.log = context.getLog();
        this.context = context;
        ReplicateConfig replicateConfig = context.getReplicateConfig();
        this.maxBatchSize = replicateConfig.getMaxBatchSize();
        this.replicateTimeout = replicateConfig.getReplicateTimeout();
        this.snapshotTimeout = replicateConfig.getSnapshotTimeout();
        this.self = context.getSelf();
        this.stateMachine = context.getStateMachine();
        this.executorService = Executors.newScheduledThreadPool(PROCESSORS + 1, new NamedThreadFactory("ReplicatePool"));
    }

    @Override
    protected void doStart() throws Exception {
        List<ClusterMember> members = context.getCluster().members();
        for (ClusterMember member : members) {
            executorService.execute(() -> replicateTo(member));
        }
    }

    @Override
    protected void doClose() {

    }

    @Override
    public CompletableFuture<Object> replicate(Entry entry) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        long index = log.append(entry);

        CompletableFuture<Object> commitFuture = new CompletableFuture<>();
        commitFuture.whenComplete((result, error) -> {
            if (error == null) {
                future.complete(result);
            } else {
                future.completeExceptionally(error);
            }
        });
        self.addCommitFuture(index,future);
        return future;
    }

    @Override
    public void replicateTo(ClusterMember member) {
        ClusterMemberState state = member.getState();
        long nextLogIndex = state.getNextLogIndex();
        if (needInstallSnapshot(nextLogIndex, state)) {
            this.snapshotTo(member);
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

    private boolean needInstallSnapshot(long nextLogIndex, ClusterMemberState member) {
        SnapshotManager snapshotManager = context.getSnapshotManager();
        Snapshot snapshot = snapshotManager.currentSnapshot();

        return snapshot != null && snapshot.index() >= nextLogIndex && snapshot.index() > member.getSnapshotIndex();
    }

    private ReplicateRequest buildReplicateRequest(ClusterMemberState member, Entry prevEntry) {
        ClusterMemberState local = context.getSelf().getState();

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


    private Entry getPrevEntry(ClusterMemberState member) {
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
        ClusterMemberState selfState = self.getState();
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

        commit(request.getCommitIndex(), selfState);

        response.setSuccess(true);
        response.setNextIndex(log.lastIndex() + 1);
        return CompletableFuture.completedFuture(response);
    }


    @Override
    public void handleReplicateResponse(ReplicateResponse response, ClusterMember member) {
        if (response.isSuccess()) {
            member.getState().setMatchedIndex(response.getNextIndex() - 1);
            replicateSuccess();
            if (!closed.get()) {
                executorService.execute(() -> replicateTo(member));
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

        ClusterMemberState state = member.getState();
        state.setNextLogIndex(response.getNextIndex());
        if (!closed.get()) {
            executorService.execute(() -> replicateTo(member));
        }
    }

    private void replicateSuccess() {
        List<ClusterMember> members = context.getCluster().members();
        members.sort(Comparator.comparingLong(member -> member.getState().getMatchedIndex()));
        int quorum = context.getCluster().getQuorum();
        if (members.size() < quorum) {
            return;
        }
        ClusterMember member = members.get(quorum - 1);
        long commitIndex = member.getState().getMatchedIndex();

        ClusterMemberState selfState = self.getState();
        long prevCommittedIndex = selfState.getCommittedIndex();
        if (commitIndex > prevCommittedIndex) {
            commit(commitIndex, selfState);
        }
    }

    private void commit(long index, ClusterMemberState memberState) {
        log.commit(Math.min(index, log.lastIndex()));
        stateMachine.apply(index);
        memberState.setCommittedIndex(index);
    }

    @Override
    public void handleReplicateResponseFailure(ClusterMember member, ReplicateRequest request, Throwable error) {
        ClusterMemberState state = member.getState();
        state.incrementReplicateFailureCount();
        logger.error("replicate data to:{} failed:{}", member.getAddress(), error);
        if (!closed.get()) {
            executorService.execute(() -> replicateTo(member));
        }
    }

    @Override
    public void snapshotTo(ClusterMember member) {
        InstallSnapshotRequest request;
        try {
            request = buildSnapshotRequest(member);
        } catch (IOException e) {
            logger.warn("build snapshot request for member:{} failed", member.getId());
            executorService.execute(() -> replicateTo(member));
            return;
        }

        InstallSnapshotRequest finalRequest = request;
        this.<InstallSnapshotResponse>request(member, request, snapshotTimeout).whenComplete((response, error) -> {
            if (error == null) {
                this.handleInstallSnapshotResponse(member, response, finalRequest);
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

        ClusterMemberState state = member.getState();
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
        CompletableFuture<InstallSnapshotResponse> future = new CompletableFuture<>();

        CompletableFuture.supplyAsync(() -> {
            InstallSnapshotResponse response = new InstallSnapshotResponse();
            ClusterMemberState selfState = self.getState();
            response.setVersion(selfState.getVersion());

            if (request.getVersion() < selfState.getVersion()) {
                response.setSuccess(false);
                future.complete(response);
                return null;
            }

            if (pendingSnapshot != null && request.getIndex() != pendingSnapshot.index()) {
                pendingSnapshot.close();
                pendingSnapshot.delete();
                pendingSnapshot = null;
                nextSnapshotOffset = 0;
            }

            if (pendingSnapshot == null) {
                if (request.getOffset() > 0) {
                    response.setSuccess(false);
                    response.setNextOffset(0);
                    future.complete(response);
                    return null;
                }
                pendingSnapshot = context.getSnapshotManager().create(request.getIndex(), System.currentTimeMillis());
                nextSnapshotOffset = 0;
            }

            if (request.getOffset() > nextSnapshotOffset) {
                response.setSuccess(false);
                response.setNextOffset(nextSnapshotOffset);
                future.complete(response);
                return null;
            }

            SnapshotWriter writer = null;
            try {
                writer = pendingSnapshot.writer();
                writer.write(request.getData());
            } finally {
                IoUtil.close(writer);
            }

            if (request.isComplete()) {
                pendingSnapshot.complete();
                stateMachine.installSnapshot(pendingSnapshot);
                pendingSnapshot = null;
                nextSnapshotOffset = 0;
            } else {
                nextSnapshotOffset += request.getData().length;
            }

            response.setSuccess(true);
            future.complete(response);
            return null;
        });

        return future;
    }

    @Override
    public void handleInstallSnapshotResponse(ClusterMember member, InstallSnapshotResponse response, InstallSnapshotRequest request) {
        if (response.getVersion() > self.getState().getVersion()) {
            //TODO 重新跟master获取联系
            return;
        }

        if (response.isSuccess()) {
            ClusterMemberState state = member.getState();
            if (request.isComplete()) {
                state.setSnapshotIndex(request.getIndex());
                state.setNextSnapshotIndex(0);
                state.setNextSnapshotOffset(0);
            } else {
                state.setNextSnapshotOffset(request.getOffset() + request.getData().length);
            }
        } else {
            ClusterMemberState state = member.getState();
            state.setNextSnapshotOffset(response.getNextOffset());
        }

        executorService.execute(() -> replicateTo(member));
    }

    @Override
    public void handleInstallSnapshotResponseFailure(ClusterMember member, InstallSnapshotRequest request, Throwable error) {
        logger.error("send snapshot to member:{} caught failure:{}", member.getId(), error);
        executorService.execute(() -> replicateTo(member));
    }
}

package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.storage.Entry;
import com.sm.finance.charge.cluster.storage.Log;
import com.sm.finance.charge.cluster.storage.snapshot.Snapshot;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotManager;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotReader;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotWriter;
import com.sm.finance.charge.common.base.LoggerSupport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author shifeng.luo
 * @version created on 2017/9/23 下午10:46
 */
public class ServerStateMachine extends LoggerSupport {

    private final Log log;
    private final ClusterMember self;
    private final FiniteStateMachine stateMachine;
    private final SnapshotManager snapshotManager;
    private final ExecutorService executorService;

    private volatile Snapshot pendingSnapshot;

    public ServerStateMachine(Log log, ClusterMember self, FiniteStateMachine stateMachine,
                              SnapshotManager snapshotManager, ExecutorService executorService) {
        this.log = log;
        this.self = self;
        this.stateMachine = stateMachine;
        this.snapshotManager = snapshotManager;
        this.executorService = executorService;
    }

    public void apply(long index) {
        ClusterMemberState state = self.getState();

        long lastApplied = state.getLastApplied();
        if (index < lastApplied + 1) {
            return;
        }

        for (long i = lastApplied + 1; i <= index; i++) {
            Entry entry = log.get(i);
            if (entry != null) {
                executorService.execute(() -> apply(entry));
            }
            setLastApplied(i);
        }
    }

    private void apply(Entry entry) {
        stateMachine.apply(entry.getCommand()).whenComplete((result, error) -> {
            CompletableFuture<Object> future = self.removeCommitFuture(entry.getIndex());
            if (future != null) {
                if (error == null) {
                    future.complete(result);
                } else {
                    future.completeExceptionally(error);
                }
            }
        });
    }

    private void setLastApplied(long lastApplied) {
        ClusterMemberState selfState = self.getState();
        if (lastApplied > selfState.getLastApplied()) {
            selfState.setLastApplied(lastApplied);

            takeSnapshot(lastApplied);
        }
    }

    private void takeSnapshot(long lastApplied) {
        if (pendingSnapshot != null) {
            return;
        }

        Compactor compactor = stateMachine.compactor();
        if (compactor.nextCompactIndex() > lastApplied) {
            return;
        }

        pendingSnapshot = snapshotManager.create(lastApplied, System.currentTimeMillis());
        logger.info("{} member taking snapshot:{}", self.getId(), pendingSnapshot.index());
        executorService.execute(() -> {
            SnapshotWriter writer = pendingSnapshot.writer();
            stateMachine.take(writer);
        });
    }


    public void installSnapshot(Snapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        long lastApplied = self.getState().getLastApplied();
        if (snapshot.index() <= lastApplied) {
            logger.error("to installed snapshot:{} less or equal to lastApplied:{}", snapshot.index(), lastApplied);
            return;
        }

        logger.info("{} install snapshot:{}", self.getId(), snapshot.index());
        executorService.execute(() -> {
            SnapshotReader reader = pendingSnapshot.reader();
            stateMachine.install(reader);
        });
        stateMachine.compactor().updateCompactIndex(snapshot.index());
    }

    static final class Result {
        final long index;
        final long eventIndex;
        final Object result;

        public Result(long index, long eventIndex, Object result) {
            this.index = index;
            this.eventIndex = eventIndex;
            this.result = result;
        }
    }
}

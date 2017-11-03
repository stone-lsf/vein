package com.sm.charge.raft.server;

import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.Snapshot;
import com.sm.charge.raft.server.storage.SnapshotManager;
import com.sm.charge.raft.server.storage.SnapshotReader;
import com.sm.charge.raft.server.storage.SnapshotWriter;
import com.sm.finance.charge.common.base.LoggerSupport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author shifeng.luo
 * @version created on 2017/9/23 下午10:46
 */
public class ServerStateMachine extends LoggerSupport {

    private final Log log;
    private final RaftMember self;
    private final LogStateMachine stateMachine;
    private final SnapshotManager snapshotManager;
    private final ExecutorService executorService;

    private volatile Snapshot pendingSnapshot;

    public ServerStateMachine(Log log, RaftMember self, LogStateMachine stateMachine,
                              SnapshotManager snapshotManager, ExecutorService executorService) {
        this.log = log;
        this.self = self;
        this.stateMachine = stateMachine;
        this.snapshotManager = snapshotManager;
        this.executorService = executorService;
    }

    public void apply(long index) {


        long lastApplied = self.getLastApplied();
        if (index < lastApplied + 1) {
            return;
        }

        for (long i = lastApplied + 1; i <= index; i++) {
            LogEntry entry = log.get(i);
            if (entry != null) {
                executorService.execute(() -> apply(entry));
            }
            setLastApplied(i);
        }
    }

    private void apply(LogEntry entry) {
        RaftMemberState context = self.getState();
        stateMachine.apply(entry.getCommand()).whenComplete((result, error) -> {
            CompletableFuture<Object> future = context.removeCommitFuture(entry.getIndex());
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
        if (lastApplied > self.getLastApplied()) {
            self.setLastApplied(lastApplied);

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
        logger.info("{} member taking snapshot:{}", self.getNodeId(), pendingSnapshot.index());
        executorService.execute(() -> {
            SnapshotWriter writer = pendingSnapshot.writer();
            stateMachine.take(writer);
        });
    }


    public void installSnapshot(Snapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        long lastApplied = self.getLastApplied();
        if (snapshot.index() <= lastApplied) {
            logger.error("to installed snapshot:{} less or equal to lastApplied:{}", snapshot.index(), lastApplied);
            return;
        }

        logger.info("{} install snapshot:{}", self.getNodeId(), snapshot.index());
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

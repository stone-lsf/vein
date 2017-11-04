package com.sm.charge.raft.server.replicate;

import com.sm.charge.raft.server.RaftMember;
import com.sm.charge.raft.server.RaftMemberState;
import com.sm.charge.raft.server.ServerContext;
import com.sm.charge.raft.server.membership.InstallSnapshotRequest;
import com.sm.charge.raft.server.membership.InstallSnapshotResponse;
import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.charge.raft.server.storage.Snapshot;
import com.sm.charge.raft.server.storage.SnapshotReader;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.handler.AbstractResponseHandler;
import com.sm.finance.charge.transport.api.support.ResponseContext;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/19 下午1:21
 */
public class Replicator extends LoggerSupport {
    private static final int MAX_BATCH_SIZE = 1024 * 32;

    private final ServerContext context;
    private final int maxAppendSize;

    public Replicator(ServerContext context, int maxAppendSize) {
        this.context = context;
        this.maxAppendSize = maxAppendSize;
    }


    public CompletableFuture<Void> replicateTo(RaftMember member) {
        logger.info("leader append entry to member:[{}]", member.getNodeId());
        Connection connection = member.getState().getConnection();
        if (connection == null) {
            return CompletableFuture.completedFuture(null);
        }

        Snapshot snapshot = context.getSnapshotManager().currentSnapshot();
        long nextLogIndex = member.getNextLogIndex();

        RaftMemberState state = member.getState();
        if (state.getInstallContext() != null) {
            return sendSnapshot(member);
        }

        if (snapshot != null && nextLogIndex < snapshot.index()) {
            InstallContext installContext = new InstallContext(snapshot);
            state.setInstallContext(installContext);
            return sendSnapshot(member);
        }

        long prevLogIndex = 0;
        long prevLogTerm = 0;
        RaftMember leader = this.context.getSelf();
        long leaderCommit = leader.getCommitIndex();
        Log log = this.context.getLog();
        LogEntry entry = log.get(nextLogIndex - 1);
        if (entry != null) {
            prevLogIndex = entry.getIndex();
            prevLogTerm = entry.getTerm();
        }

        LogEntry lastLogEntry = log.lastEntry();
        long lastLogIndex = lastLogEntry == null ? 0 : lastLogEntry.getIndex();

        AppendRequest request = new AppendRequest();
        request.setDestination(member.getNodeId());
        request.setSource(leader.getNodeId());
        request.setTerm(leader.getTerm());
        request.setPrevLogIndex(prevLogIndex);
        request.setPrevLogTerm(prevLogTerm);
        request.setLeaderCommit(leaderCommit);

        long endIndex = Math.min(lastLogIndex, prevLogIndex + 1 + maxAppendSize);
        ArrayList<LogEntry> entries = new ArrayList<>();
        for (; nextLogIndex < endIndex; nextLogIndex++) {
            LogEntry logEntry = log.get(nextLogIndex);
            if (logEntry != null) {
                entries.add(logEntry);
            }
        }

        request.setEntries(entries);

        CompletableFuture<Void> future = new CompletableFuture<>();
        connection.send(request, new AbstractResponseHandler<AppendResponse>() {
            @Override
            public void handle(AppendResponse response, Connection connection) {
                context.getEventExecutor().execute(response).whenComplete((result, error) -> {
                    if (error != null) {
                        logger.error("handle append response:{} caught exception", response, error);
                    }
                    future.complete(null);
                });
            }

            @Override
            public void onException(Throwable e, ResponseContext context) {
                logger.error("send append request to member:{} caught exception", member.getNodeId(), e);
                future.complete(null);
            }
        });
        return future;
    }

    private CompletableFuture<Void> sendSnapshot(RaftMember member) {
        RaftMemberState state = member.getState();
        InstallContext installContext = state.getInstallContext();
        InstallSnapshotRequest request = buildSnapshotRequest(installContext, member);

        CompletableFuture<Void> future = new CompletableFuture<>();
        Connection connection = state.getConnection();
        if (connection == null) {
            future.complete(null);
            return future;
        }

        connection.send(request, new AbstractResponseHandler<InstallSnapshotResponse>() {
            @Override
            public void handle(InstallSnapshotResponse response, Connection connection) {
                context.getEventExecutor().execute(response).whenComplete((result, error) -> {
                    if (error != null) {
                        logger.error("handle install response:{} caught exception", response, error);
                    }
                    future.complete(null);
                });
            }

            @Override
            public void onException(Throwable e, ResponseContext context) {
                logger.error("send install request to:{} caught exception", member.getNodeId(), e);
                future.complete(null);
            }
        });

        return future;
    }


    private InstallSnapshotRequest buildSnapshotRequest(InstallContext installContext, RaftMember member) {
        Snapshot snapshot = installContext.getSnapshot();
        long offset = installContext.getOffset();
        SnapshotReader reader = snapshot.reader();
        reader.skip(offset);

        byte[] data = new byte[Math.min(MAX_BATCH_SIZE, (int) reader.remaining())];
        reader.read(data);
        installContext.setSize(data.length);

        InstallSnapshotRequest request = new InstallSnapshotRequest();

        request.setIndex(snapshot.index());
        request.setTerm(context.getSelf().getTerm());
        request.setDestination(member.getNodeId());
        request.setOffset(offset);
        request.setData(data);
        request.setComplete(!reader.hasRemaining());
        installContext.setComplete(!reader.hasRemaining());

        return request;
    }
}

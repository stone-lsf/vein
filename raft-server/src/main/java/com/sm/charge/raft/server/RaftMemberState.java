package com.sm.charge.raft.server;

import com.sm.charge.raft.server.state.support.VoteQuorum;
import com.sm.charge.raft.server.state.support.SnapshotInstallContext;
import com.sm.charge.raft.server.state.support.ReplicateTask;
import com.sm.charge.raft.server.state.support.Replicator;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午10:50
 */
public class RaftMemberState extends LoggerSupport {
    private final TransportClient client;

    private final RaftMember member;

    private final ReplicateTask replicateTask;

    private volatile Connection connection;

    private volatile Future<?> appendFuture;

    private volatile VoteQuorum voteQuorum;

    /**
     * 是否配置中
     */
    private volatile long configuring;

    private volatile SnapshotInstallContext installContext;

    /**
     * 需要复制的下一个snapshot的index
     */
    private volatile long nextSnapshotIndex;

    /**
     * 需要复制的下一个snapshot的offset
     */
    private volatile long nextSnapshotOffset;

    private final ConcurrentMap<Long, CompletableFuture<Object>> commitFutures = new ConcurrentHashMap<>();

    public RaftMemberState(TransportClient client, RaftMember member, Replicator replicator) {
        this.client = client;
        this.member = member;
        this.replicateTask = new ReplicateTask(member, replicator);
    }

    public Connection getConnection() {
        if (connection != null && !connection.closed()) {
            return connection;
        }

        Address address = member.getAddress();
        return client.connect(address).handle((conn, error) -> {
            if (error == null) {
                connection = conn;
                return conn;
            }

            logger.error("create connection to:{} caught exception", address, error);
            return null;
        }).join();
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void startReplicate() {
        replicateTask.start();
    }

    public void stopReplicate() {
        replicateTask.stop();
    }

    public void addCommitFuture(long logIndex, CompletableFuture<Object> future) {
        commitFutures.put(logIndex, future);
    }


    public CompletableFuture<Object> removeCommitFuture(long logIndex) {
        return commitFutures.remove(logIndex);
    }

    public Future<?> getAppendFuture() {
        return appendFuture;
    }

    public void setAppendFuture(Future<?> appendFuture) {
        this.appendFuture = appendFuture;
    }

    public VoteQuorum getVoteQuorum() {
        return voteQuorum;
    }

    public void setVoteQuorum(VoteQuorum voteQuorum) {
        this.voteQuorum = voteQuorum;
    }

    public void clearVoteQuorum() {
        this.voteQuorum.cancel();
        this.voteQuorum = null;
    }

    public long getConfiguring() {
        return configuring;
    }

    public void setConfiguring(long configuring) {
        this.configuring = configuring;
    }

    public long getNextSnapshotIndex() {
        return nextSnapshotIndex;
    }

    public void setNextSnapshotIndex(long nextSnapshotIndex) {
        this.nextSnapshotIndex = nextSnapshotIndex;
    }

    public long getNextSnapshotOffset() {
        return nextSnapshotOffset;
    }

    public void setNextSnapshotOffset(long nextSnapshotOffset) {
        this.nextSnapshotOffset = nextSnapshotOffset;
    }

    public SnapshotInstallContext getInstallContext() {
        return installContext;
    }

    public void setInstallContext(SnapshotInstallContext installContext) {
        this.installContext = installContext;
    }
}

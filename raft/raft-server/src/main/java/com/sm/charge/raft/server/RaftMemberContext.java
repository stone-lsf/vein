package com.sm.charge.raft.server;

import com.sm.charge.raft.server.election.VoteQuorum;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.LogSupport;
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
public class RaftMemberContext extends LogSupport {
    private final TransportClient client;

    private final long id;

    private final Address address;

    private volatile Connection connection;

    private volatile Future<?> appendFuture;

    private volatile VoteQuorum voteQuorum;

    private final ConcurrentMap<Long, CompletableFuture<Object>> commitFutures = new ConcurrentHashMap<>();

    public RaftMemberContext(TransportClient client, long id, Address address) {
        this.client = client;
        this.id = id;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public Connection getConnection() {
        if (connection != null && !connection.closed()) {
            return connection;
        }

        return client.connect(address).handle((connection, error) -> {
            if (error != null) {
                return connection;
            }

            logger.error("create connection to:{} caught exception", address, error);
            return null;
        }).join();
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
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
}

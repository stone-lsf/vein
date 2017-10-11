package com.sm.charge.raft.server;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午10:50
 */
public class RaftMember {
    private final TransportClient client;

    private final long id;

    private final Address address;

    private final RaftMemberState state;

    private volatile Connection connection;

    private final ConcurrentMap<Long, CompletableFuture<Object>> commitFutures = new ConcurrentHashMap<>();

    public RaftMember(TransportClient client, long id, Address address) {
        this.client = client;
        this.id = id;
        this.address = address;
        this.state = new RaftMemberState(this);
    }

    public long getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public RaftMemberState getState() {
        return state;
    }

    public CompletableFuture<Connection> getConnection() {
        if (connection == null) {
            return client.connect(address);
        }
        return CompletableFuture.completedFuture(connection);
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
}

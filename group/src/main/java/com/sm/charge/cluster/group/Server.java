package com.sm.charge.cluster.group;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午10:28
 */
public class Server {

    private Address address;
    private volatile long commitIndex;
    private volatile long matchIndex;
    private volatile long replicateIndex;
    private volatile Connection connection;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public long getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(long matchIndex) {
        this.matchIndex = matchIndex;
    }

    public long getReplicateIndex() {
        return replicateIndex;
    }

    public void setReplicateIndex(long replicateIndex) {
        this.replicateIndex = replicateIndex;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}

package com.sm.finance.charge.cluster;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午10:50
 */
public class ClusterMember {

    private final long id;

    private final Address address;

    private final MemberState state;

    private volatile Connection connection;


    public ClusterMember(long id, Address address) {
        this.id = id;
        this.address = address;
        this.state = new MemberState(this);
    }

    public long getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public MemberState getState() {
        return state;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}

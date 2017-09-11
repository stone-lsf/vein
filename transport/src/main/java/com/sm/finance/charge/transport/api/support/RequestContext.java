package com.sm.finance.charge.transport.api.support;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:34
 */
public class RequestContext {

    private int requestId;

    private Connection connection;

    private Address remote;


    public RequestContext(int requestId, Connection connection, Address remote) {
        this.requestId = requestId;
        this.connection = connection;
        this.remote = remote;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Address getRemote() {
        return remote;
    }

    public void setRemote(Address remote) {
        this.remote = remote;
    }
}

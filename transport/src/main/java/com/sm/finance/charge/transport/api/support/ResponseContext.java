package com.sm.finance.charge.transport.api.support;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:40
 */
public class ResponseContext {

    private Connection connection;

    private Exception exception;

    private Address remote;

    public ResponseContext(Connection connection, Address remote) {
        this.connection = connection;
        this.remote = remote;
    }

    public ResponseContext(Connection connection, Exception exception, Address remote) {
        this.connection = connection;
        this.exception = exception;
        this.remote = remote;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Address getRemote() {
        return remote;
    }

    public void setRemote(Address remote) {
        this.remote = remote;
    }
}

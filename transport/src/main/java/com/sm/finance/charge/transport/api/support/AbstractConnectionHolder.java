package com.sm.finance.charge.transport.api.support;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionHolder;

/**
 * @author shifeng.luo
 * @version created on 2017/11/11 上午11:50
 */
public class AbstractConnectionHolder implements ConnectionHolder {

    protected final Address address;

    /**
     * 连接
     */
    private volatile Connection connection;

    public AbstractConnectionHolder(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
        connection.addCloseListener(this);
    }

    @Override
    public void clearConnection() {
        this.connection = null;
    }
}

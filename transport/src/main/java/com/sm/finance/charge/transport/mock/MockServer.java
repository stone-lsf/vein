package com.sm.finance.charge.transport.mock;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.utils.AddressUtil;
import com.sm.finance.charge.transport.api.ConnectionListener;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.TransportServer;
import com.sm.finance.charge.transport.api.exceptions.BindException;
import com.sm.finance.charge.transport.api.support.DefaultConnectionManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 上午11:53
 */
public class MockServer implements TransportServer {

    private AtomicBoolean listened = new AtomicBoolean(false);
    private ConnectionManager connectionManager = new DefaultConnectionManager();
    private MockNetwork network = MockNetwork.buildInstance();
    private volatile Address bindAddress;

    @Override
    public void close() throws Exception {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public void close(int timeout) throws Exception {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void listen(int port, ConnectionListener listener) throws BindException {
        if (listened.compareAndSet(false, true)) {
            this.bindAddress = AddressUtil.getLocalAddress(port);
            network.registerServer(this);
        }
    }

    @Override
    public Address getBindAddress() {
        return bindAddress;
    }
}

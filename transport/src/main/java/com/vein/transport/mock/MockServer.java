package com.vein.transport.mock;

import com.vein.common.Address;
import com.vein.common.utils.AddressUtil;
import com.vein.transport.api.ConnectionListener;
import com.vein.transport.api.ConnectionManager;
import com.vein.transport.api.TransportServer;
import com.vein.transport.api.exceptions.BindException;
import com.vein.transport.api.support.DefaultConnectionManager;

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
        connectionManager.closeAll();
        network.removeServer(this);
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
        connectionManager.closeAll();
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

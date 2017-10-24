package com.sm.finance.charge.transport.mock;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.IntegerIdGenerator;
import com.sm.finance.charge.common.utils.AddressUtil;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.exceptions.ConnectException;
import com.sm.finance.charge.transport.api.support.AbstractClient;
import com.sm.finance.charge.transport.api.support.DefaultConnectionManager;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 上午11:52
 */
public class MockClient extends AbstractClient {

    private static final IntegerIdGenerator portGenerator = new IntegerIdGenerator(40000);
    private final MockNetwork network = MockNetwork.buildInstance();
    private ConnectionManager connectionManager = new DefaultConnectionManager();

    @Override
    protected void doClose() throws Exception {

    }

    @Override
    protected void doStart() throws Exception {

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
    public CompletableFuture<Connection> connect(Address address) {
        CompletableFuture<Connection> future = new CompletableFuture<>();
        try {
            MockConnection connection = network.registerConnect(this, address);
            return CompletableFuture.completedFuture(connection);
        } catch (ConnectException e) {
            future.completeExceptionally(e);
            return future;
        }
    }

    Address getUsableAddress() {
        return AddressUtil.getLocalAddress(portGenerator.nextId());
    }
}

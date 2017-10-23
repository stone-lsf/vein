package com.sm.finance.charge.transport.mock;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.IntegerIdGenerator;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.common.utils.AddressUtil;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.exceptions.ConnectException;
import com.sm.finance.charge.transport.api.support.DefaultConnectionManager;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 上午11:52
 */
public class MockClient extends LoggerSupport implements TransportClient {

    private static final IntegerIdGenerator portGenerator = new IntegerIdGenerator(40000);
    private final MockNetwork network = MockNetwork.buildInstance();
    private ConnectionManager connectionManager = new DefaultConnectionManager();

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
    public CompletableFuture<Connection> connect(Address address) {
        CompletableFuture<Connection> future = new CompletableFuture<>();
        MockConnection connection = null;
        try {
            connection = network.registerConnect(this, address);
        } catch (ConnectException e) {
            future.completeExceptionally(e);
            return future;
        }
        return CompletableFuture.completedFuture(connection);
    }

    @Override
    public CompletableFuture<Connection> connect(Address address, int retryTimes) {
        CompletableFuture<Connection> result = new CompletableFuture<>();

        connect(address).whenComplete((connection, error) -> {
            if (error != null) {
                logger.warn("connect to address:[{}] caught exception:{}", address, error);
                if (retryTimes > 0) {
                    connect(address, retryTimes - 1).whenComplete((conn, e) -> {
                        if (e == null) {
                            result.complete(conn);
                        } else {
                            result.completeExceptionally(e);
                        }
                    });
                } else {
                    result.complete(null);
                }
            } else {
                result.complete(connection);
            }
        });

        return result;
    }

    public Address getUsableAddress() {
        return AddressUtil.getLocalAddress(portGenerator.nextId());
    }
}

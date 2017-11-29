package com.vein.transport.mock;

import com.vein.common.Address;
import com.vein.common.base.LoggerSupport;
import com.vein.transport.api.Connection;
import com.vein.transport.api.Request;
import com.vein.transport.api.Response;
import com.vein.transport.api.exceptions.ConnectException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 上午11:53
 */
public class MockNetwork extends LoggerSupport {

    private static final MockNetwork instance = new MockNetwork();
    private ConcurrentMap<Address, MockServer> serverMap = new ConcurrentHashMap<>();

    private ConcurrentMap<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, String> connectionIdPair = new ConcurrentHashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    private MockNetwork() {
    }

    static MockNetwork buildInstance() {
        return instance;
    }

    void registerServer(MockServer server) {
        serverMap.put(server.getBindAddress(), server);
    }

    MockConnection registerConnect(MockClient client, Address serverAddress) throws ConnectException {
        MockServer server = serverMap.get(serverAddress);
        if (server == null) {
            throw new ConnectException("can't connect to com.sm.charge.raft.server:" + serverAddress);
        }

        Address clientAddress = client.getUsableAddress();
        MockConnection connection = new MockConnection(clientAddress, serverAddress);
        connectionMap.put(connection.getConnectionId(), connection);
        connection.setNetwork(this);

        MockConnection serverConnection = new MockConnection(serverAddress, clientAddress);
        server.getConnectionManager().addConnection(serverConnection);
        connectionMap.put(serverConnection.getConnectionId(), serverConnection);
        serverConnection.setNetwork(this);

        connectionIdPair.put(connection.getConnectionId(), serverConnection.getConnectionId());
        connectionIdPair.put(serverConnection.getConnectionId(), connection.getConnectionId());

        return connection;
    }


    void sendRequest(MockConnection connection, Request request) {
        String id = connection.getConnectionId();
        String pairId = connectionIdPair.get(id);
        Connection connectionPair = connectionMap.get(pairId);
        if (connectionPair != null) {
            executorService.execute(() -> connectionPair.onMessage(request));
        }
    }

    void sendResponse(MockConnection connection, Response response) {
        String id = connection.getConnectionId();
        String pairId = connectionIdPair.get(id);
        Connection connectionPair = connectionMap.get(pairId);
        if (connectionPair != null) {
            executorService.execute(() -> connectionPair.onMessage(response));
        }
    }

    void removeConnection(MockConnection connection) throws Exception {
        connectionMap.remove(connection.getConnectionId());
        String pairId = connectionIdPair.get(connection.getConnectionId());
        if (pairId != null) {
            Connection connectionPair = connectionMap.get(pairId);
            if (connectionPair != null) {
                connectionPair.close();
            }
        }
    }

    void removeServer(MockServer server) throws Exception {
        serverMap.remove(server.getBindAddress());
    }
}

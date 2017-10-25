package com.sm.charge.memory;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.sm.finance.charge.common.SystemConstants.PROCESSORS;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:44
 */
public class ServerContext extends LoggerSupport {

    /**
     * 节点唯一标识符
     */
    private final String nodeId;

    /**
     * 连接客户端
     */
    private final TransportClient client;

    /**
     * 连接服务端
     */
    private final TransportServer server;

    private final ScheduledExecutorService executorService;

    private final ConcurrentMap<Address, Connection> remoteConnections = new ConcurrentHashMap<>();

    ServerContext(String nodeId, TransportClient client, TransportServer server) {
        this.nodeId = nodeId;
        this.client = client;
        this.server = server;
        this.executorService = Executors.newScheduledThreadPool(PROCESSORS + 1, new NamedThreadFactory("DiscoveryPool"));
    }

    public String getNodeId() {
        return nodeId;
    }

    public TransportClient getClient() {
        return client;
    }

    public TransportServer getServer() {
        return server;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public Connection getConnection(Address address) {
        return remoteConnections.get(address);
    }

    public Connection createConnection(Address address) {
        return createConnection(address, 0);
    }

    public Connection createConnection(Address address, int retryTimes) {
        Connection connection = remoteConnections.get(address);
        if (connection != null && !connection.closed()) {
            return connection;
        }

        return client.connect(address, retryTimes).handle((con, error) -> {
            if (error == null) {
                Connection exist = remoteConnections.putIfAbsent(address, con);
                if (exist == null) {
                    con.addCloseListener(() -> remoteConnections.remove(address));
                } else {
                    try {
                        con.close();
                    } catch (Exception e) {
                        logger.warn("close connection:{} caught exception", con.getConnectionId(), e);
                    }
                    con = exist;
                }
                return con;
            }

            logger.error("create connection to:{} caught exception", address, error);
            return null;
        }).join();
    }


}

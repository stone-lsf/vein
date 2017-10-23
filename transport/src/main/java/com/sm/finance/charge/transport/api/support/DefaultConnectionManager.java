package com.sm.finance.charge.transport.api.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.sm.finance.charge.common.base.CloseListener;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.handler.RequestHandler;

import java.util.List;
import java.util.Map;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:24
 */
public class DefaultConnectionManager implements ConnectionManager {

    private final Map<String, Connection> connections = Maps.newHashMap();

    private final List<RequestHandler> handlers = Lists.newArrayList();

    @Override
    public synchronized void registerMessageHandler(RequestHandler handler) {
        handlers.add(handler);
    }

    @Override
    public synchronized void addConnection(Connection connection) {
        connection.addCloseListener(new ManagerCloseListener(connection));
        connection.registerHandlers(handlers);
        connections.put(connection.getConnectionId(), connection);
    }

    @Override
    public synchronized Connection getConnection(String connectionId) {
        return connections.get(connectionId);
    }

    @Override
    public synchronized Connection removeConnection(String connectionId) {
        return connections.remove(connectionId);
    }

    @Override
    public synchronized List<Connection> getAll() {
        return Lists.newArrayList(connections.values());
    }

    @Override
    public synchronized void closeAll() throws Exception {
        for (String connectionId : connections.keySet()) {
            Connection connection = connections.get(connectionId);
            connection.close();
        }
    }


    private class ManagerCloseListener implements CloseListener {
        private final Connection connection;

        ManagerCloseListener(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void onClose() {
            DefaultConnectionManager.this.removeConnection(connection.getConnectionId());
        }
    }
}

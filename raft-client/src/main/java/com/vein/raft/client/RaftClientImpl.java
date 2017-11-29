package com.vein.raft.client;

import com.vein.common.Address;
import com.vein.common.base.LoggerSupport;
import com.vein.serializer.api.Serializer;
import com.vein.transport.api.Connection;
import com.vein.transport.api.Transport;
import com.vein.transport.api.TransportFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/13 下午1:03
 */
public class RaftClientImpl extends LoggerSupport implements RaftClient {

    private List<Address> clusters;
    private Transport transport;
    private ClientConnection connection;

    public RaftClientImpl() {
        this.transport = TransportFactory.create("netty");
        this.connection = new ClientConnection(transport.client());
    }

    public RaftClientImpl(List<Address> clusters) {
        this.clusters = clusters;
        this.transport = TransportFactory.create("netty");
        this.connection = new ClientConnection(transport.client(), clusters);
    }

    @Override
    public void start() {
        connection.setClusters(clusters);
        Connection conn = connection.connect().handle((connection, error) -> {
            if (error == null) {
                return connection;
            }
            logger.error("connect to cluster failed!", error);
            return null;
        }).join();

        if (conn == null) {
            throw new RuntimeException("connect to cluster failed!");
        }
    }

    @Override
    public Serializer serializer() {
        return null;
    }

    @Override
    public <T> CompletableFuture<T> submit(Command command) {
        return connection.request(command);
    }

    @Override
    public void register(Watcher watcher) {

    }

    @Override
    public void close() {

    }

    public void setClusters(List<Address> clusters) {
        this.clusters = clusters;
    }
}

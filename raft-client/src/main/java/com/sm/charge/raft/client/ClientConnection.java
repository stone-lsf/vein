package com.sm.charge.raft.client;

import com.sm.charge.raft.client.protocal.ConnectRequest;
import com.sm.charge.raft.client.protocal.ConnectResponse;
import com.sm.charge.raft.client.protocal.RaftResponse;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;

import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.sm.charge.raft.client.protocal.RaftResponse.SUCCESS;

/**
 * @author shifeng.luo
 * @version created on 2017/11/18 上午11:55
 */
public class ClientConnection extends LoggerSupport {

    private TransportClient client;
    private volatile Connection connection;
    private AtomicReference<CompletableFuture<Connection>> connectFuture = new AtomicReference<>();
    private Address leader;
    private volatile List<Address> clusters;
    private volatile int index;

    public ClientConnection(TransportClient client, List<Address> clusters) {
        this.client = client;
        this.clusters = clusters;
    }

    public Address getLeader() {
        return leader;
    }

    public <T> CompletableFuture<T> request(Object request) {
        CompletableFuture<T> future = new CompletableFuture<>();
        connect().whenComplete((connection, error) -> {
            if (error == null) {
                connection.<T>request(request).whenComplete((response, e) -> {
                    if (e == null) {
                        future.complete(response);
                    } else {
                        logger.error("send request:{} caught exception", request, e);
                        future.completeExceptionally(e);
                    }
                });
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    public CompletableFuture<Void> send(Object request) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        connect().whenComplete((connection, error) -> {
            if (error == null) {
                try {
                    connection.send(request);
                } catch (IOException e) {
                    logger.error("send request:{} caught exception", request, e);
                    future.completeExceptionally(e);
                }
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }


    CompletableFuture<Connection> connect() {
        if (connection != null) {
            return CompletableFuture.completedFuture(connection);
        }

        if (!connectFuture.compareAndSet(null, new CompletableFuture<>())) {
            return connectFuture.get();
        }

        CompletableFuture<Connection> future = connectFuture.get();
        if (CollectionUtils.isEmpty(clusters)) {
            logger.error("cluster is empty");
            future.completeExceptionally(new RuntimeException("cluster is empty"));
            return future;
        }

        connect(future);
        return future;
    }

    private void connect(CompletableFuture<Connection> future) {
        if (index > clusters.size()) {
            logger.error("fail to connect to cluster");
            future.completeExceptionally(new RuntimeException("fail to connect to cluster"));
        } else {
            Address address = clusters.get(index);
            index++;
            client.connect(address).whenComplete((connection, error) -> {
                if (error != null) {
                    connect(future);
                } else {
                    connectCluster(connection, future);
                }
            });
        }
    }

    private void connectCluster(Connection connection, CompletableFuture<Connection> future) {
        connection.addCloseListener(() -> {
            this.connection = null;
        });

        ConnectRequest request = new ConnectRequest();
        connection.<RaftResponse<ConnectResponse>>request(request).whenComplete((response, error) -> {
            if (error != null) {
                logger.error("send connect request:{} by connection:{} failed", request, connection.getConnectionId());
                connect(future);
            } else {
                if (response.getStatus() == SUCCESS) {
                    ConnectResponse data = response.getData();
                    this.leader = data.getLeader();
                    this.clusters = data.getMembers();
                    this.index = 0;
                    future.complete(connection);
                } else {
                    connect(future);
                }
            }
        });
    }


}

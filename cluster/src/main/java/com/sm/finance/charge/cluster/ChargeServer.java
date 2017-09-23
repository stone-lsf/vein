package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.client.Command;
import com.sm.finance.charge.cluster.discovery.DefaultDiscoveryService;
import com.sm.finance.charge.cluster.discovery.DiscoveryConfig;
import com.sm.finance.charge.cluster.discovery.DiscoveryService;
import com.sm.finance.charge.cluster.replicate.ReplicateRequest;
import com.sm.finance.charge.cluster.storage.Log;
import com.sm.finance.charge.cluster.storage.entry.Entry;
import com.sm.finance.charge.common.AbstractService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:14
 */
public class ChargeServer extends AbstractService implements ClusterServer {

    private DiscoveryService discoveryService;
    private String clusterName;
    private ClusterConfig clusterConfig;
    private ConcurrentMap<Long, CompletableFuture<Object>> commitFutures = new ConcurrentHashMap<>();
    private Log log;
    private Cluster cluster;


    public ChargeServer() {
        DiscoveryConfig discoveryConfig = new DiscoveryConfig();
        discoveryService = new DefaultDiscoveryService(discoveryConfig);
    }

    @Override
    public CompletableFuture<Boolean> join() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        discoveryService.join(clusterName);

        return future;
    }

    @Override
    public void send(Object message) {
        ReplicateRequest data = new ReplicateRequest();
    }

    @Override
    public CompletableFuture<Object> handle(Command command) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        Entry entry = new Entry(command);
        long index = log.append(entry);

        CompletableFuture<Object> commitFuture = new CompletableFuture<>();
        commitFuture.whenComplete((result, error) -> {
            if (error == null) {
                future.complete(result);
            } else {
                future.completeExceptionally(error);
            }
        });
        commitFutures.put(index, commitFuture);
        return future;
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {

    }
}

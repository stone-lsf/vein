package com.sm.finance.charge.cluster;

import com.sm.charge.memory.DiscoveryService;
import com.sm.finance.charge.cluster.client.Command;
import com.sm.finance.charge.cluster.elect.MasterListener;
import com.sm.finance.charge.cluster.replicate.DefaultReplicateService;
import com.sm.finance.charge.cluster.replicate.ReplicateService;
import com.sm.finance.charge.cluster.storage.Entry;
import com.sm.finance.charge.common.AbstractService;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:14
 */
public class ChargeServer extends AbstractService implements ClusterServer, MasterListener {

    private final DiscoveryService discoveryService;
    private final ReplicateService replicateService;
    private final Cluster cluster;


    public ChargeServer(DiscoveryService discoveryService, ServerContext serverContext) {
        this.discoveryService = discoveryService;
        this.replicateService = new DefaultReplicateService(serverContext);
        this.cluster = serverContext.getCluster();
    }

    public CompletableFuture<Boolean> join() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        discoveryService.join(cluster.name());

        return future;
    }

    @Override
    public CompletableFuture<Boolean> leave() {
        return null;
    }

    @Override
    public CompletableFuture<Object> handle(Command command) {
        Entry entry = new Entry(command, cluster.version());
        return replicateService.replicate(entry);
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {

    }

    @Override
    public void onMaster() {
        try {
            replicateService.start();
        } catch (Exception e) {
            logger.error("start replicate service failure", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void offMaster() {
        try {
            replicateService.close();
        } catch (Exception e) {
            logger.error("stop replicate service failure", e);
            throw new RuntimeException(e);
        }
    }
}

package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.client.Command;
import com.sm.finance.charge.cluster.discovery.DefaultDiscoveryService;
import com.sm.finance.charge.cluster.discovery.DiscoveryConfig;
import com.sm.finance.charge.cluster.discovery.DiscoveryService;
import com.sm.finance.charge.cluster.replicate.MajorReplicateArbitrator;
import com.sm.finance.charge.cluster.replicate.ReplicateRequest;
import com.sm.finance.charge.cluster.replicate.ReplicateResponse;
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
//        data.setPayload(message);

        MajorReplicateArbitrator arbitrator = new MajorReplicateArbitrator(clusterConfig.getCandidateCount(), data);
//        data.setNotifier(arbitrator::start);

        //TODO 设置arbitrator监听器

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
    public CompletableFuture<ReplicateResponse> replicate(ClusterMember member) {
        MemberState state = member.getState();
        long nextLogIndex = state.getNextLogIndex();
        long version = cluster.version();

        long committedIndex = cluster.master().getState().getCommittedIndex();
//        log.

        return null;
    }


    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {

    }
}

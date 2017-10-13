package com.sm.charge.raft.server;

import com.sm.charge.memory.DiscoveryService;
import com.sm.charge.raft.client.Command;
import com.sm.charge.raft.server.election.MasterListener;
import com.sm.charge.raft.server.membership.JoinRequest;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.membership.LeaveRequest;
import com.sm.charge.raft.server.membership.LeaveResponse;
import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.finance.charge.common.AbstractService;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:14
 */
public class RaftServerImpl extends AbstractService implements RaftServer, MasterListener {

    private final DiscoveryService discoveryService;
    private final RaftCluster cluster;


    public RaftServerImpl(DiscoveryService discoveryService, ServerContext serverContext) {
        this.discoveryService = discoveryService;
        this.cluster = serverContext.getCluster();
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void transition(RaftState state) {

    }

    public CompletableFuture<Boolean> join() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        discoveryService.join(cluster.name());

        return future;
    }

    @Override
    public CompletableFuture<JoinResponse> handle(JoinRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> leave() {
        return null;
    }

    @Override
    public CompletableFuture<LeaveResponse> handle(LeaveRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Object> handle(Command command) {
        LogEntry entry = new LogEntry(command, cluster.version());

        return null;
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
        } catch (Exception e) {
            logger.error("start replicate service failure", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void offMaster() {
        try {
        } catch (Exception e) {
            logger.error("stop replicate service failure", e);
            throw new RuntimeException(e);
        }
    }
}

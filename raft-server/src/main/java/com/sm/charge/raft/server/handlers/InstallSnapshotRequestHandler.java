package com.sm.charge.raft.server.handlers;

import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.membership.InstallSnapshotRequest;
import com.sm.charge.raft.server.membership.InstallSnapshotResponse;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/4 下午6:43
 */
public class InstallSnapshotRequestHandler extends AbstractRequestHandler<InstallSnapshotRequest> {
    private final RaftServer raftServer;

    public InstallSnapshotRequestHandler(RaftServer raftServer) {
        this.raftServer = raftServer;
    }

    @Override
    public CompletableFuture<InstallSnapshotResponse> handle(InstallSnapshotRequest request, RequestContext context) throws Exception {
        return raftServer.handle(request);
    }
}

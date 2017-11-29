package com.vein.raft.server.handlers;

import com.vein.raft.server.RaftServer;
import com.vein.raft.server.events.InstallSnapshotRequest;
import com.vein.raft.server.events.InstallSnapshotResponse;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

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

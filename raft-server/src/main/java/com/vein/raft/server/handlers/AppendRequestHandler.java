package com.vein.raft.server.handlers;

import com.vein.raft.server.RaftServer;
import com.vein.raft.server.events.AppendRequest;
import com.vein.raft.server.events.AppendResponse;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/4 下午3:50
 */
public class AppendRequestHandler extends AbstractRequestHandler<AppendRequest> {

    private final RaftServer raftServer;

    public AppendRequestHandler(RaftServer raftServer) {
        this.raftServer = raftServer;
    }

    @Override
    public CompletableFuture<AppendResponse> handle(AppendRequest request, RequestContext context) throws Exception {
        return raftServer.handle(request);
    }
}

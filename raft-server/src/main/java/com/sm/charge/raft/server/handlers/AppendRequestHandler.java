package com.sm.charge.raft.server.handlers;

import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.replicate.AppendRequest;
import com.sm.charge.raft.server.replicate.AppendResponse;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

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

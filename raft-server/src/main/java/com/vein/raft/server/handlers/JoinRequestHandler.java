package com.vein.raft.server.handlers;

import com.vein.raft.server.RaftServer;
import com.vein.raft.server.events.JoinRequest;
import com.vein.raft.server.events.JoinResponse;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/4 下午3:50
 */
public class JoinRequestHandler extends AbstractRequestHandler<JoinRequest>{

    private final RaftServer raftServer;

    public JoinRequestHandler(RaftServer raftServer) {
        this.raftServer = raftServer;
    }

    @Override
    public CompletableFuture<JoinResponse> handle(JoinRequest request, RequestContext context) throws Exception {
        return raftServer.handle(request,context);
    }
}

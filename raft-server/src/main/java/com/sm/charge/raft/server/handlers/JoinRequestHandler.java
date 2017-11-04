package com.sm.charge.raft.server.handlers;

import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.membership.JoinRequest;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

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

package com.vein.raft.server.handlers;

import com.vein.raft.server.RaftServer;
import com.vein.raft.server.events.VoteRequest;
import com.vein.raft.server.events.VoteResponse;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/4 下午3:50
 */
public class VoteRequestHandler extends AbstractRequestHandler<VoteRequest> {

    private final RaftServer raftServer;

    public VoteRequestHandler(RaftServer raftServer) {
        this.raftServer = raftServer;
    }

    @Override
    public CompletableFuture<VoteResponse> handle(VoteRequest request, RequestContext context) throws Exception {
        return raftServer.handle(request);
    }
}

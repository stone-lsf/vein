package com.vein.raft.server.handlers;

import com.vein.raft.server.RaftServer;
import com.vein.raft.server.events.LeaveRequest;
import com.vein.raft.server.events.LeaveResponse;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/4 下午3:50
 */
public class LeaveRequestHandler extends AbstractRequestHandler<LeaveRequest> {

    private final RaftServer raftServer;

    public LeaveRequestHandler(RaftServer raftServer) {
        this.raftServer = raftServer;
    }

    @Override
    public CompletableFuture<LeaveResponse> handle(LeaveRequest request, RequestContext context) throws Exception {
        return raftServer.handle(request, context);
    }
}

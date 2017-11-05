package com.sm.charge.raft.server.handlers;

import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.events.LeaveRequest;
import com.sm.charge.raft.server.events.LeaveResponse;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

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

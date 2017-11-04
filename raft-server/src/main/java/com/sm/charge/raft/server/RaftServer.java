package com.sm.charge.raft.server;

import com.sm.charge.raft.client.Command;
import com.sm.charge.raft.server.election.VoteRequest;
import com.sm.charge.raft.server.election.VoteResponse;
import com.sm.charge.raft.server.membership.InstallSnapshotRequest;
import com.sm.charge.raft.server.membership.InstallSnapshotResponse;
import com.sm.charge.raft.server.membership.JoinRequest;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.membership.LeaveRequest;
import com.sm.charge.raft.server.membership.LeaveResponse;
import com.sm.charge.raft.server.replicate.AppendRequest;
import com.sm.charge.raft.server.replicate.AppendResponse;
import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:45
 */
public interface RaftServer extends Startable, Closable {

    String getId();

    /**
     * 加入集群
     *
     * @return 成功则返回true，否则返回false
     */
    boolean join();

    CompletableFuture<JoinResponse> handle(JoinRequest request, RequestContext context);

    CompletableFuture<Boolean> leave();

    CompletableFuture<LeaveResponse> handle(LeaveRequest request, RequestContext context);

    CompletableFuture<VoteResponse> handle(VoteRequest request);

    CompletableFuture<AppendResponse> handle(AppendRequest request);

    CompletableFuture<InstallSnapshotResponse> handle(InstallSnapshotRequest request);

    CompletableFuture<Object> handle(Command command);
}

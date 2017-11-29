package com.vein.raft.server;

import com.vein.raft.client.protocal.CommandRequest;
import com.vein.raft.client.protocal.RaftResponse;
import com.vein.raft.server.events.AppendRequest;
import com.vein.raft.server.events.AppendResponse;
import com.vein.raft.server.events.InstallSnapshotRequest;
import com.vein.raft.server.events.InstallSnapshotResponse;
import com.vein.raft.server.events.JoinRequest;
import com.vein.raft.server.events.JoinResponse;
import com.vein.raft.server.events.LeaveRequest;
import com.vein.raft.server.events.LeaveResponse;
import com.vein.raft.server.events.VoteRequest;
import com.vein.raft.server.events.VoteResponse;
import com.vein.common.base.Closable;
import com.vein.common.base.Startable;
import com.vein.transport.api.support.RequestContext;

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

    <T> CompletableFuture<RaftResponse<T>> handle(CommandRequest request);
}

package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftState;
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
import com.sm.finance.charge.transport.api.support.RequestContext;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:32
 */
public interface ServerState {

    RaftState state();

    /**
     * 挂起
     */
    void suspect();

    /**
     * 唤醒
     */
    void wakeup();

    VoteResponse handle(VoteRequest request);

    void handle(VoteResponse response);

    AppendResponse handle(AppendRequest request);

    void handle(AppendResponse response);

    JoinResponse handle(JoinRequest request, RequestContext requestContext);

    void handle(JoinResponse response);

    LeaveResponse handle(LeaveRequest request,RequestContext requestContext);

    void handle(LeaveResponse response);

    InstallSnapshotResponse handle(InstallSnapshotRequest request);

    void handle(InstallSnapshotResponse response);
}

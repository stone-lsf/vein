package com.vein.raft.server.state;

import com.vein.raft.server.RaftState;
import com.vein.raft.server.events.VoteRequest;
import com.vein.raft.server.events.VoteResponse;
import com.vein.raft.server.events.InstallSnapshotRequest;
import com.vein.raft.server.events.InstallSnapshotResponse;
import com.vein.raft.server.events.JoinRequest;
import com.vein.raft.server.events.JoinResponse;
import com.vein.raft.server.events.LeaveRequest;
import com.vein.raft.server.events.LeaveResponse;
import com.vein.raft.server.events.AppendRequest;
import com.vein.raft.server.events.AppendResponse;
import com.vein.transport.api.support.RequestContext;

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

    LeaveResponse handle(LeaveRequest request, RequestContext requestContext);

    void handle(LeaveResponse response);

    InstallSnapshotResponse handle(InstallSnapshotRequest request);

    void handle(InstallSnapshotResponse response);
}

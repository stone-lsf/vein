package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftState;
import com.sm.charge.raft.server.election.VoteRequest;
import com.sm.charge.raft.server.election.VoteResponse;
import com.sm.charge.raft.server.membership.ConfigurationRequest;
import com.sm.charge.raft.server.membership.ConfigurationResponse;
import com.sm.charge.raft.server.membership.InstallSnapshotRequest;
import com.sm.charge.raft.server.membership.InstallSnapshotResponse;
import com.sm.charge.raft.server.membership.JoinRequest;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.replicate.AppendRequest;
import com.sm.charge.raft.server.replicate.AppendResponse;

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

    JoinResponse handle(JoinRequest request);

    void handle(JoinResponse response);

    ConfigurationResponse handle(ConfigurationRequest request);

    void handle(ConfigurationResponse response);

    InstallSnapshotResponse handle(InstallSnapshotRequest request);

    void handle(InstallSnapshotResponse response);
}

package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.election.VoteRequest;
import com.sm.charge.raft.server.election.VoteResponse;
import com.sm.charge.raft.server.membership.ConfigurationRequest;
import com.sm.charge.raft.server.membership.ConfigurationResponse;
import com.sm.charge.raft.server.membership.InstallSnapshotRequest;
import com.sm.charge.raft.server.membership.InstallSnapshotResponse;
import com.sm.charge.raft.server.replicate.AppendRequest;
import com.sm.charge.raft.server.replicate.AppendResponse;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:32
 */
public interface RaftService {

    void handle(VoteRequest request);

    void handle(VoteResponse response);

    void handle(AppendRequest request);

    void handle(AppendResponse response);

    void handle(ConfigurationRequest request);

    void handle(ConfigurationResponse response);

    void handle(InstallSnapshotRequest request);

    void handle(InstallSnapshotResponse response);
}

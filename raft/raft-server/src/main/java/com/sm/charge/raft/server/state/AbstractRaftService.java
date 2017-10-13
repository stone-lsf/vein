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
 * @version created on 2017/10/13 下午4:25
 */
public abstract class AbstractRaftService implements RaftService {
    @Override
    public void handle(VoteRequest request) {

    }

    @Override
    public void handle(VoteResponse response) {

    }

    @Override
    public void handle(AppendRequest request) {

    }

    @Override
    public void handle(AppendResponse response) {

    }

    @Override
    public void handle(ConfigurationRequest request) {

    }

    @Override
    public void handle(ConfigurationResponse response) {

    }

    @Override
    public void handle(InstallSnapshotRequest request) {

    }

    @Override
    public void handle(InstallSnapshotResponse response) {

    }
}

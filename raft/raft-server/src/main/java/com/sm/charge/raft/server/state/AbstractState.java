package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.election.VoteEvent;
import com.sm.charge.raft.server.election.VoteResponse;
import com.sm.charge.raft.server.membership.ConfigurationEvent;
import com.sm.charge.raft.server.membership.ConfigurationResponse;
import com.sm.charge.raft.server.membership.JoinEvent;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.membership.LeaveEvent;
import com.sm.charge.raft.server.membership.LeaveResponse;
import com.sm.charge.raft.server.replicate.AppendEvent;
import com.sm.charge.raft.server.replicate.AppendResponse;
import com.sm.charge.raft.server.storage.snapshot.Snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:49
 */
public abstract class AbstractState implements ServerState, RaftState {

    @Override
    public void handle(VoteEvent event) {

    }

    @Override
    public void handle(VoteResponse response) {

    }

    @Override
    public void handle(JoinEvent event) {

    }

    @Override
    public void handle(JoinResponse response) {

    }

    @Override
    public void handle(LeaveEvent event) {

    }

    @Override
    public void handle(LeaveResponse response) {

    }

    @Override
    public void handle(AppendEvent event) {

    }

    @Override
    public void handle(AppendResponse response) {

    }

    @Override
    public void handle(ConfigurationEvent event) {

    }

    @Override
    public void handle(ConfigurationResponse response) {

    }

    @Override
    public RaftState onInstallComplete(Snapshot snapshot, RaftServer raftServer) {
        throw new IllegalStateException();
    }

    @Override
    public RaftState onElectTimeout(RaftServer raftServer) {
        throw new IllegalStateException();
    }

    @Override
    public RaftState onElectAsMaster(RaftServer raftServer) {
        throw new IllegalStateException();
    }

    @Override
    public RaftState onNewLeader(RaftServer raftServer) {
        throw new IllegalStateException();
    }

    @Override
    public RaftState onFallBehind(RaftServer raftServer) {
        throw new IllegalStateException();
    }
}

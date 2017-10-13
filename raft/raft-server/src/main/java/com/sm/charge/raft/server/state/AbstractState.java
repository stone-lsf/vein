package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.storage.snapshot.Snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:49
 */
public abstract class AbstractState extends AbstractRaftService implements RaftState {

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

package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.storage.snapshot.Snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:15
 */
public interface RaftState {

    String name();

    RaftState onInstallComplete(Snapshot snapshot, RaftServer raftServer);

    RaftState onElectTimeout(RaftServer raftServer);

    RaftState onElectAsMaster(RaftServer raftServer);

    RaftState onNewLeader(RaftServer raftServer);

    RaftState onFallBehind(RaftServer raftServer);
}

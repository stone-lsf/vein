package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.storage.snapshot.Snapshot;
import com.sm.finance.charge.common.LogSupport;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:15
 */
public class RaftStateMachine extends LogSupport {

    private final RaftServer raftServer;

    private volatile RaftState state;

    public RaftStateMachine(RaftServer raftServer, RaftState state) {
        this.raftServer = raftServer;
        this.state = state;
    }

    private void onInstallComplete(Snapshot snapshot) {
        logger.info("server[{}] install snapshot:{} completed，current state[{}]", raftServer.getId(), snapshot.index(), state.name());
        state = state.onInstallComplete(snapshot, raftServer);
    }

    private void onElectTimeout() {
        logger.info("server[{}] elect timeout，current state[{}]", raftServer.getId(), state.name());
        state = state.onElectTimeout(raftServer);
    }

    private void onElectAsMaster() {
        logger.info("server[{}] elect as master，current state[{}]", raftServer.getId(), state.name());
        state = state.onElectAsMaster(raftServer);
    }

    private void onNewLeader() {
        logger.info("server[{}] found new master，current state[{}]", raftServer.getId(), state.name());
        state = state.onNewLeader(raftServer);
    }

    private void onFallBehind() {
        logger.info("server[{}] found higher term server，current state[{}]", raftServer.getId(), state.name());
        state = state.onFallBehind(raftServer);
    }
}

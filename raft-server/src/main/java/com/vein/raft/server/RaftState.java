package com.vein.raft.server;

import com.vein.raft.server.storage.snapshot.Snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 上午1:27
 */
public enum RaftState {

    PASSIVE {
        @Override
        public RaftState onInstallComplete(Snapshot snapshot, ServerContext raftServer) {
            raftServer.transition(FOLLOWER);
            return FOLLOWER;
        }
    },
    FOLLOWER {
        @Override
        public RaftState onHeartbeatTimeout(ServerContext raftServer) {
            raftServer.transition(CANDIDATE);
            return CANDIDATE;
        }

        @Override
        public RaftState onFallBehind(ServerContext raftServer) {
            raftServer.transition(FOLLOWER);
            return FOLLOWER;
        }
    },
    CANDIDATE {
        @Override
        public RaftState onElectTimeout(ServerContext raftServer) {
            raftServer.transition(CANDIDATE);
            return CANDIDATE;
        }

        @Override
        public RaftState onElectAsMaster(ServerContext raftServer) {
            raftServer.transition(LEADER);
            return LEADER;
        }

        @Override
        public RaftState onNewLeader(ServerContext raftServer) {
            raftServer.transition(FOLLOWER);
            return FOLLOWER;
        }

        @Override
        public RaftState onFallBehind(ServerContext raftServer) {
            raftServer.transition(FOLLOWER);
            return FOLLOWER;
        }
    },
    LEADER {
        @Override
        public RaftState onFallBehind(ServerContext raftServer) {
            raftServer.transition(FOLLOWER);
            return FOLLOWER;
        }
    };

    public RaftState onInstallComplete(Snapshot snapshot, ServerContext raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onHeartbeatTimeout(ServerContext raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onElectTimeout(ServerContext raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onElectAsMaster(ServerContext raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onNewLeader(ServerContext raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onFallBehind(ServerContext raftServer) {
        throw new IllegalStateException();
    }
}

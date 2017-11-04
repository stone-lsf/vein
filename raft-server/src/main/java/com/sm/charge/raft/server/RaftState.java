package com.sm.charge.raft.server;

import com.sm.charge.raft.server.storage.Snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 上午1:27
 */
public enum RaftState {

    PASSIVE {
        @Override
        public RaftState onInstallComplete(Snapshot snapshot, RaftServer raftServer) {
            return super.onInstallComplete(snapshot, raftServer);
        }
    },
    FOLLOWER {
        @Override
        public RaftState onElectTimeout(RaftServer raftServer) {
            return super.onElectTimeout(raftServer);
        }

        @Override
        public RaftState onFallBehind(RaftServer raftServer) {
            return super.onFallBehind(raftServer);
        }
    },
    CANDIDATE {
        @Override
        public RaftState onElectTimeout(RaftServer raftServer) {
            return super.onElectTimeout(raftServer);
        }

        @Override
        public RaftState onElectAsMaster(RaftServer raftServer) {
            return super.onElectAsMaster(raftServer);
        }

        @Override
        public RaftState onNewLeader(RaftServer raftServer) {
            return super.onNewLeader(raftServer);
        }

        @Override
        public RaftState onFallBehind(RaftServer raftServer) {
            return super.onFallBehind(raftServer);
        }
    },
    LEADER {
        @Override
        public RaftState onFallBehind(RaftServer raftServer) {
            return super.onFallBehind(raftServer);
        }
    };

    public RaftState onInstallComplete(Snapshot snapshot, RaftServer raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onElectTimeout(RaftServer raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onElectAsMaster(RaftServer raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onNewLeader(RaftServer raftServer) {
        throw new IllegalStateException();
    }

    public RaftState onFallBehind(RaftServer raftServer) {
        throw new IllegalStateException();
    }
}

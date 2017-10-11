package com.sm.charge.raft.server;


import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.snapshot.SnapshotManager;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午11:00
 */
public class ServerContext {

    private final RaftConfig raftConfig;

    private final SnapshotManager snapshotManager;

    private final Log log;

    private final RaftMember self;

    private final RaftClusterImpl cluster;

    private final ServerStateMachine stateMachine;

    private ServerContext(RaftConfig raftConfig, SnapshotManager snapshotManager, Log log,
                          RaftMember self, RaftClusterImpl cluster, ServerStateMachine stateMachine) {
        this.raftConfig = raftConfig;
        this.snapshotManager = snapshotManager;
        this.log = log;
        this.self = self;
        this.cluster = cluster;
        this.stateMachine = stateMachine;
    }

    public RaftClusterImpl getCluster() {
        return cluster;
    }


    public SnapshotManager getSnapshotManager() {
        return snapshotManager;
    }


    public Log getLog() {
        return log;
    }


    public RaftMember getSelf() {
        return self;
    }


    public ServerStateMachine getStateMachine() {
        return stateMachine;
    }

    public RaftConfig getRaftConfig() {
        return raftConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RaftConfig raftConfig;

        private SnapshotManager snapshotManager;

        private Log log;

        private RaftMember self;

        private RaftClusterImpl cluster;

        private ServerStateMachine stateMachine;

        public Builder setRaftConfig(RaftConfig raftConfig) {
            this.raftConfig = raftConfig;
            return this;
        }

        public Builder setSnapshotManager(SnapshotManager snapshotManager) {
            this.snapshotManager = snapshotManager;
            return this;
        }

        public Builder setLog(Log log) {
            this.log = log;
            return this;
        }

        public Builder setSelf(RaftMember self) {
            this.self = self;
            return this;
        }

        public Builder setCluster(RaftClusterImpl cluster) {
            this.cluster = cluster;
            return this;
        }

        public Builder setStateMachine(ServerStateMachine stateMachine) {
            this.stateMachine = stateMachine;
            return this;
        }

        public ServerContext build() {
            return new ServerContext(raftConfig, snapshotManager, log, self, cluster, stateMachine);
        }
    }
}

package com.sm.charge.raft.server;


import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.MemberStateManager;
import com.sm.charge.raft.server.storage.SnapshotManager;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午11:00
 */
public class ServerContext {

    private final RaftConfig raftConfig;

    private final SnapshotManager snapshotManager;

    private final Log log;

    private final RaftMember self;

    private final RaftCluster cluster;

    private final ServerStateMachine stateMachine;

    private final MemberStateManager memberStateManager;

    private ServerContext(RaftConfig raftConfig, SnapshotManager snapshotManager, Log log,
                          RaftMember self, RaftCluster cluster, ServerStateMachine stateMachine, MemberStateManager memberStateManager) {
        this.raftConfig = raftConfig;
        this.snapshotManager = snapshotManager;
        this.log = log;
        this.self = self;
        this.cluster = cluster;
        this.stateMachine = stateMachine;
        this.memberStateManager = memberStateManager;
    }

    public RaftCluster getCluster() {
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

    public MemberStateManager getMemberStateManager() {
        return memberStateManager;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RaftConfig raftConfig;

        private SnapshotManager snapshotManager;

        private Log log;

        private RaftMember self;

        private RaftCluster cluster;

        private ServerStateMachine stateMachine;

        private MemberStateManager memberStateManager;

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

        public Builder setCluster(RaftCluster cluster) {
            this.cluster = cluster;
            return this;
        }

        public Builder setStateMachine(ServerStateMachine stateMachine) {
            this.stateMachine = stateMachine;
            return this;
        }

        public Builder setMemberStateManager(MemberStateManager memberStateManager) {
            this.memberStateManager = memberStateManager;
            return this;
        }


        public ServerContext build() {
            return new ServerContext(raftConfig, snapshotManager, log, self, cluster, stateMachine, memberStateManager);
        }
    }
}

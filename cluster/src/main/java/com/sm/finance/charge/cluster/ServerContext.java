package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.replicate.ReplicateConfig;
import com.sm.finance.charge.cluster.storage.Log;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotManager;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午11:00
 */
public class ServerContext {

    private final ReplicateConfig replicateConfig;

    private final SnapshotManager snapshotManager;

    private final Log log;

    private final ClusterMember self;

    private final ChargeCluster cluster;

    private final ServerStateMachine stateMachine;

    private ServerContext(ReplicateConfig replicateConfig, SnapshotManager snapshotManager, Log log,
                          ClusterMember self, ChargeCluster cluster, ServerStateMachine stateMachine) {
        this.replicateConfig = replicateConfig;
        this.snapshotManager = snapshotManager;
        this.log = log;
        this.self = self;
        this.cluster = cluster;
        this.stateMachine = stateMachine;
    }

    public ChargeCluster getCluster() {
        return cluster;
    }


    public SnapshotManager getSnapshotManager() {
        return snapshotManager;
    }


    public Log getLog() {
        return log;
    }


    public ClusterMember getSelf() {
        return self;
    }


    public ServerStateMachine getStateMachine() {
        return stateMachine;
    }

    public ReplicateConfig getReplicateConfig() {
        return replicateConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ReplicateConfig replicateConfig;

        private SnapshotManager snapshotManager;

        private Log log;

        private ClusterMember self;

        private ChargeCluster cluster;

        private ServerStateMachine stateMachine;

        public Builder setReplicateConfig(ReplicateConfig replicateConfig) {
            this.replicateConfig = replicateConfig;
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

        public Builder setSelf(ClusterMember self) {
            this.self = self;
            return this;
        }

        public Builder setCluster(ChargeCluster cluster) {
            this.cluster = cluster;
            return this;
        }

        public Builder setStateMachine(ServerStateMachine stateMachine) {
            this.stateMachine = stateMachine;
            return this;
        }

        public ServerContext build() {
            return new ServerContext(replicateConfig, snapshotManager, log, self, cluster, stateMachine);
        }
    }
}

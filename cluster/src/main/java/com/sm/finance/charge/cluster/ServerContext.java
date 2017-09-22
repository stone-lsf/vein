package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.storage.Log;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotManager;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午11:00
 */
public class ServerContext {

    private SnapshotManager snapshotManager;

    private Log log;

    private ClusterMember member;

    private Cluster cluster;

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public SnapshotManager getSnapshotManager() {
        return snapshotManager;
    }

    public void setSnapshotManager(SnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public ClusterMember getMember() {
        return member;
    }

    public void setMember(ClusterMember member) {
        this.member = member;
    }
}

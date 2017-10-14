package com.sm.charge.raft.server;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午10:52
 */
public class RaftConfig {

    /**
     * 复制日志超时时间(ms)
     */
    private int replicateTimeout;

    /**
     * 候选节点数量
     */
    private int candidateCount;

    /**
     * 集群名称
     */
    private String clusterName;

    private int port;
    private String transportType;


    private String snapshotDrectory;
    private String snapshotName;

    public int getReplicateTimeout() {
        return replicateTimeout;
    }

    public void setReplicateTimeout(int replicateTimeout) {
        this.replicateTimeout = replicateTimeout;
    }

    public int getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(int candidateCount) {
        this.candidateCount = candidateCount;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSnapshotDrectory() {
        return snapshotDrectory;
    }

    public void setSnapshotDrectory(String snapshotDrectory) {
        this.snapshotDrectory = snapshotDrectory;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }
}

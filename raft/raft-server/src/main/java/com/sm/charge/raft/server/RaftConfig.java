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
     * 集群名称
     */
    private String clusterName;

    private int port;
    private String transportType;


    private String snapshotDirectory;
    private String snapshotName;

    private int heartbeatTimeout;

    private int maxElectTimeout;

    private int minElectTimeout;

    private String members;

    private int joinRetryTimes;

    private int maxAppendSize;

    public int getReplicateTimeout() {
        return replicateTimeout;
    }

    public void setReplicateTimeout(int replicateTimeout) {
        this.replicateTimeout = replicateTimeout;
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

    public String getSnapshotDirectory() {
        return snapshotDirectory;
    }

    public void setSnapshotDirectory(String snapshotDirectory) {
        this.snapshotDirectory = snapshotDirectory;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public int getMaxElectTimeout() {
        return maxElectTimeout;
    }

    public void setMaxElectTimeout(int maxElectTimeout) {
        this.maxElectTimeout = maxElectTimeout;
    }

    public int getMinElectTimeout() {
        return minElectTimeout;
    }

    public void setMinElectTimeout(int minElectTimeout) {
        this.minElectTimeout = minElectTimeout;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public int getJoinRetryTimes() {
        return joinRetryTimes;
    }

    public void setJoinRetryTimes(int joinRetryTimes) {
        this.joinRetryTimes = joinRetryTimes;
    }

    public int getMaxAppendSize() {
        return maxAppendSize;
    }

    public void setMaxAppendSize(int maxAppendSize) {
        this.maxAppendSize = maxAppendSize;
    }
}

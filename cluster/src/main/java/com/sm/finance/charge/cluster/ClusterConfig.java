package com.sm.finance.charge.cluster;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午10:52
 */
public class ClusterConfig {

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
}

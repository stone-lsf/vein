package com.sm.finance.charge.cluster.replicate;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:43
 */
public class ReplicateConfig {

    private int maxBatchSize;

    private int replicateTimeout;

    private int snapshotTimeout;

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getReplicateTimeout() {
        return replicateTimeout;
    }

    public void setReplicateTimeout(int replicateTimeout) {
        this.replicateTimeout = replicateTimeout;
    }

    public int getSnapshotTimeout() {
        return snapshotTimeout;
    }

    public void setSnapshotTimeout(int snapshotTimeout) {
        this.snapshotTimeout = snapshotTimeout;
    }
}

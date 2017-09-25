package com.sm.finance.charge.cluster.storage.snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午7:36
 */
public interface SnapshotWriter extends AutoCloseable {

    void write(byte[] data);
}

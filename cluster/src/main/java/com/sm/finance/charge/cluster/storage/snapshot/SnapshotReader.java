package com.sm.finance.charge.cluster.storage.snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午2:31
 */
public interface SnapshotReader extends AutoCloseable {

    long remaining();

    SnapshotReader read(byte[] bytes);

    boolean hasRemaining();
}

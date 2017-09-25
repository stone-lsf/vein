package com.sm.finance.charge.cluster.storage.snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午7:21
 */
public interface Snapshot {

    long index();

    long timestamp();

    Snapshot complete();

    void close();

    void delete();

    SnapshotReader reader();

    SnapshotWriter writer();
}

package com.sm.finance.charge.cluster.storage.snapshot;

import java.util.Collection;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午10:44
 */
public interface SnapshotManager {

    Snapshot currentSnapshot();

    Collection<Snapshot> snapshots();

    Snapshot snapshot(long index);
}

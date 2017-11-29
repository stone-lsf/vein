package com.vein.raft.server.storage.snapshot;

import com.vein.common.base.Closable;
import com.vein.common.base.Startable;

import java.util.Collection;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午10:44
 */
public interface SnapshotManager extends Startable, Closable {

    Snapshot create(long index);

    void addSnapshot(Snapshot snapshot);

    Snapshot currentSnapshot();

    Collection<Snapshot> snapshots();

    Snapshot snapshot(long index);
}

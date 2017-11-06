package com.sm.charge.raft.server.storage.snapshot;

import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

import java.util.Collection;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午10:44
 */
public interface SnapshotManager extends Startable, Closable {

    Snapshot create(long index);

    Snapshot currentSnapshot();

    Collection<Snapshot> snapshots();

    Snapshot snapshot(long index);
}

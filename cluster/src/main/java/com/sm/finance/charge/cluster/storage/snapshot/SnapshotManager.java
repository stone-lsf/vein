package com.sm.finance.charge.cluster.storage.snapshot;

import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.Startable;

import java.io.File;
import java.util.Collection;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午10:44
 */
public interface SnapshotManager extends Startable,Closable{

    Snapshot create(long index,long timestamp);

    SnapshotDescriptor parse(File file);

    Snapshot currentSnapshot();

    Collection<Snapshot> snapshots();

    Snapshot snapshot(long index);
}

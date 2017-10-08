package com.sm.finance.charge.cluster.storage.snapshot.file;

import com.sm.finance.charge.cluster.storage.snapshot.Snapshot;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotDescriptor;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotReader;
import com.sm.finance.charge.cluster.storage.snapshot.SnapshotWriter;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午5:05
 */
public class FileSnapshot implements Snapshot {
    private final File file;
    private final long index;
    private final long timestamp;

    FileSnapshot(SnapshotDescriptor descriptor, File file) {
        this.index = descriptor.index();
        this.timestamp = descriptor.timestamp();
        this.file = file;
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public long index() {
        return index;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public Snapshot complete() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void delete() {

    }

    @Override
    public SnapshotReader reader() {
        return null;
    }

    @Override
    public SnapshotWriter writer() {
        return null;
    }
}

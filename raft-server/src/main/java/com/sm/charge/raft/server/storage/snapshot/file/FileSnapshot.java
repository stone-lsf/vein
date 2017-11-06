package com.sm.charge.raft.server.storage.snapshot.file;


import com.sm.charge.raft.server.storage.snapshot.Snapshot;
import com.sm.charge.raft.server.storage.snapshot.SnapshotReader;
import com.sm.charge.raft.server.storage.snapshot.SnapshotWriter;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午5:05
 */
public class FileSnapshot implements Snapshot {
    private final File file;
    private final long index;
    private final String createTime;

    FileSnapshot(File file, long index, String createTime) {
        this.index = index;
        this.createTime = createTime;
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
    public String createTime() {
        return createTime;
    }

    @Override
    public Snapshot complete() {
        return null;
    }

    @Override
    public void check() {

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

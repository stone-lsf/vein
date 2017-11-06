package com.sm.charge.raft.server.storage.snapshot;

import com.sm.charge.buffer.BufferOutputStream;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午7:36
 */
public interface SnapshotWriter extends BufferOutputStream<SnapshotWriter> {

    File getFile();

    SnapshotWriter position(int position);

    long position();

    SnapshotWriter writeObject(Object object);

    SnapshotWriter trimToValidSize();
}

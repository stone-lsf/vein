package com.sm.charge.raft.server.storage.snapshot;

import com.sm.charge.buffer.BufferOutputStream;
import com.sm.finance.charge.serializer.api.Serializable;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午7:36
 */
public interface SnapshotWriter extends BufferOutputStream<SnapshotWriter> {

    File getFile();

    SnapshotWriter skip(int size);

    long position();

    SnapshotWriter writeObject(Serializable object);

    SnapshotWriter trimToValidSize();

    void close();
}

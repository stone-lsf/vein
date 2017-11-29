package com.vein.raft.server.storage.snapshot;

import com.vein.buffer.BufferOutputStream;
import com.vein.serializer.api.Serializable;

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

    /**
     * close writer
     */
    @Override
    void close();
}

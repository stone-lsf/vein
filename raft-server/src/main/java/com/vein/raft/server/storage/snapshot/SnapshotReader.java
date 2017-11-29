package com.vein.raft.server.storage.snapshot;

import com.vein.buffer.BufferInputStream;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午2:31
 */
public interface SnapshotReader extends BufferInputStream<SnapshotReader> {

    File getFile();

    /**
     * close reader
     */
    @Override
    void close();
}

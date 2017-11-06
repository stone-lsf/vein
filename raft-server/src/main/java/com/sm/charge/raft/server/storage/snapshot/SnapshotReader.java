package com.sm.charge.raft.server.storage.snapshot;

import com.sm.charge.buffer.BufferInputStream;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午2:31
 */
public interface SnapshotReader extends BufferInputStream<SnapshotReader> {

    File getFile();

    void close();
}

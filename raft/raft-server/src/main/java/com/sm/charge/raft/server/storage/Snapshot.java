package com.sm.charge.raft.server.storage;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午7:21
 */
public interface Snapshot {

    File file();

    long index();

    long timestamp();

    Snapshot complete();

    void close();

    void delete();

    SnapshotReader reader();

    SnapshotWriter writer();
}

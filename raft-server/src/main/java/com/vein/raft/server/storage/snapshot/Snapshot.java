package com.vein.raft.server.storage.snapshot;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午7:21
 */
public interface Snapshot {

    File file();

    long index();

    String createTime();

    Snapshot complete();

    void check();

    void delete();

    SnapshotReader reader();

    SnapshotWriter writer();
}

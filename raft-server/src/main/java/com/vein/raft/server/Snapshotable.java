package com.vein.raft.server;


import com.vein.raft.server.storage.snapshot.SnapshotReader;
import com.vein.raft.server.storage.snapshot.SnapshotWriter;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午5:00
 */
public interface Snapshotable {

    void take(SnapshotWriter writer);

    void install(SnapshotReader reader);
}

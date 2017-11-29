package com.vein.charge.core;

import com.vein.raft.client.Command;
import com.vein.raft.server.Compactor;
import com.vein.raft.server.LogStateMachine;
import com.vein.raft.server.storage.snapshot.SnapshotReader;
import com.vein.raft.server.storage.snapshot.SnapshotWriter;
import com.vein.serializer.api.Serializer;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/22 上午11:15
 */
public class PrintLogStateMachine implements LogStateMachine {
    @Override
    public void take(SnapshotWriter writer) {

    }

    @Override
    public void install(SnapshotReader reader) {

    }

    @Override
    public <T> CompletableFuture<T> apply(Command command) {
        return null;
    }

    @Override
    public Compactor compactor() {
        return null;
    }

    @Override
    public Serializer getSerializer() {
        return null;
    }
}

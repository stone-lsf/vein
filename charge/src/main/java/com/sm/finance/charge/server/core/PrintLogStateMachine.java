package com.sm.finance.charge.server.core;

import com.sm.charge.raft.client.Command;
import com.sm.charge.raft.server.Compactor;
import com.sm.charge.raft.server.LogStateMachine;
import com.sm.charge.raft.server.storage.SnapshotReader;
import com.sm.charge.raft.server.storage.SnapshotWriter;

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
}

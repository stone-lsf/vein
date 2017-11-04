package com.sm.charge.raft.server;


import com.sm.charge.raft.client.Command;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午11:24
 */
public interface LogStateMachine extends Snapshotable {

    <T> CompletableFuture<T> apply(Command command);

    Compactor compactor();
}

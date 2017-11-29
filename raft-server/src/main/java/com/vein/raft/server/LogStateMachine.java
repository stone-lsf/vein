package com.vein.raft.server;


import com.vein.raft.client.Command;
import com.vein.serializer.api.Serializer;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午11:24
 */
public interface LogStateMachine extends Snapshotable {

    <T> CompletableFuture<T> apply(Command command);

    Compactor compactor();

    Serializer getSerializer();
}

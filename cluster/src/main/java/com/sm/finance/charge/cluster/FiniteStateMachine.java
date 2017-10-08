package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.client.Command;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午11:24
 */
public interface FiniteStateMachine extends Snapshotable {

    <T> CompletableFuture<T> apply(Command command);

    Compactor compactor();
}

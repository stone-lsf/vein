package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.storage.entry.Entry;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午11:24
 */
public interface StateMachine {

    <T> CompletableFuture<T> apply(Entry entry);
}

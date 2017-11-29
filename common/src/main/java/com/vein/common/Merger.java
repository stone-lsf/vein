package com.vein.common;

import com.vein.common.base.LoggerSupport;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午5:11
 */
public class Merger extends LoggerSupport {

    private final int capacity;

    private Counter successCounter;
    private Counter failureCounter;

    public Merger(int capacity) {
        this.capacity = capacity;
    }


    public CompletableFuture<Boolean> anyOf() {
        return innerGt(0);
    }

    public CompletableFuture<Boolean> ge(int num) {
        return innerGe(num);
    }

    private CompletableFuture<Boolean> innerGe(int num) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        successCounter = new Counter(count -> {
            if (count >= num) {
                future.complete(true);
            }
        });

        failureCounter = new Counter(count -> {
            if (count + successCounter.getCount() >= capacity) {
                future.complete(false);
            }
        });

        return future;
    }

    private CompletableFuture<Boolean> innerGt(int num) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        successCounter = new Counter(count -> {
            if (count > num) {
                future.complete(true);
            }
        });

        failureCounter = new Counter(count -> {
            if (count + successCounter.getCount() >= capacity) {
                future.complete(false);
            }
        });
        return future;
    }


    public void mergeSuccess() {
        successCounter.increase();
    }

    public void mergeFailure() {
        failureCounter.increase();
    }
}

package com.sm.finance.charge.common;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午5:11
 */
public class Merger extends LogSupport {

    private final int capacity;

    private Counter successCounter;
    private Counter failureCounter;

    public Merger(int capacity) {
        this.capacity = capacity;
        this.failureCounter = new Counter(Counter.Listener.empty);
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
            } else if (count + failureCounter.getCount() >= capacity) {
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
            } else if (count + failureCounter.getCount() >= capacity) {
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

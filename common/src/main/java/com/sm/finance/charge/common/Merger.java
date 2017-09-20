package com.sm.finance.charge.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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


    public boolean anyOf() throws Exception {
        CompletableFuture<Boolean> future = innerGt(0);

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("merge  failure,caught exception", e);
            throw new Exception(e);
        }
    }

    public boolean ge(int num) throws Exception {
        CompletableFuture<Boolean> future = innerGe(num);

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("merge  failure,caught exception", e);
            throw new Exception(e);
        }
    }

    public boolean ge(int num, int timeout) throws Exception {
        CompletableFuture<Boolean> future = innerGe(num);

        return future.get(timeout, TimeUnit.MILLISECONDS);
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

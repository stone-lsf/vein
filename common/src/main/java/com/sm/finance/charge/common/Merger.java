package com.sm.finance.charge.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        successCounter = new Counter(count -> {
            if (count > 0) {
                future.complete(true);
            } else if (count + failureCounter.getCount() == capacity) {
                future.complete(false);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("merge  failure,caught exception", e);
            throw new Exception(e);
        }
    }

    public void mergeSuccess() {
        successCounter.increase();
    }

    public void mergeFailure() {
        failureCounter.increase();
    }
}

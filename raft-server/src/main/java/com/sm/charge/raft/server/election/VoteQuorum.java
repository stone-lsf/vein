package com.sm.charge.raft.server.election;

import com.sm.finance.charge.common.Counter;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午11:40
 */
public class VoteQuorum {

    private final int quorum;

    private Counter successCounter;
    private Counter failureCounter;

    private volatile boolean cancelled = false;

    public VoteQuorum(int quorum) {
        this.quorum = quorum;

    }

    public void cancel() {
        cancelled = true;
    }

    public CompletableFuture<Boolean> getResult() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        successCounter = new Counter(count -> {
            if (count >= quorum) {
                future.complete(true);
            }
        });

        failureCounter = new Counter(count -> {
            if (count >= quorum) {
                future.complete(false);
            }
        });

        return future;
    }

    public void mergeSuccess() {
        if (!cancelled) {
            successCounter.increase();
        }
    }

    public void mergeFailure() {
        if (!cancelled) {
            failureCounter.increase();
        }
    }
}

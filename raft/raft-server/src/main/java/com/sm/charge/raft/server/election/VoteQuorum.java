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

    public VoteQuorum(int quorum) {
        this.quorum = quorum;

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
        successCounter.increase();
    }

    public void mergeFailure() {
        failureCounter.increase();
    }
}

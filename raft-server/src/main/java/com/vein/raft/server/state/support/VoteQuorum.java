package com.vein.raft.server.state.support;

import com.vein.common.Counter;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午11:40
 */
public class VoteQuorum {

    private CompletableFuture<Boolean> future = new CompletableFuture<>();
    private Counter successCounter;
    private Counter failureCounter;

    private volatile boolean cancelled = false;

    public VoteQuorum(int quorum, VoteCallback callback) {
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

        future.whenComplete((success, error) -> {
            if (success) {
                callback.onSuccess();
            }
        });
    }

    public void cancel() {
        cancelled = true;
        future.cancel(false);
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

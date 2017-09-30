package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.storage.Log;
import com.sm.finance.charge.cluster.storage.entry.Entry;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/23 下午10:46
 */
public class ServerStateMachine implements StateMachine {

    private final Log log;
    private final ClusterMember self;

    public ServerStateMachine(Log log, ClusterMember self) {
        this.log = log;
        this.self = self;
    }


    @Override
    public <T> CompletableFuture<T> apply(Entry entry) {
        CompletableFuture<T> future = new CompletableFuture<>();
        //TODO execute command

        MemberState state = self.getState();
        state.setLastApplied(entry.getIndex());

        return future;
    }


    static final class Result {
        final long index;
        final long eventIndex;
        final Object result;

        public Result(long index, long eventIndex, Object result) {
            this.index = index;
            this.eventIndex = eventIndex;
            this.result = result;
        }
    }
}

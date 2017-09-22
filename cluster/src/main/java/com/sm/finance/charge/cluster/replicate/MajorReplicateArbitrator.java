package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.Merger;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午2:00
 */
public class MajorReplicateArbitrator extends LogSupport implements ReplicateArbitrator {

    private CopyOnWriteArrayList<ArbitrateListener> listeners = new CopyOnWriteArrayList<>();
    private final int minSuccessNum;
    private final Merger merger;
    private final ReplicateRequest replicateData;

    public MajorReplicateArbitrator(int replicateCount, ReplicateRequest replicateData) {
        this.minSuccessNum = (replicateCount + 1) / 2;
        this.merger = new Merger(replicateCount);
        this.replicateData = replicateData;
    }

    @Override
    public void flagOneFail() {
        merger.mergeFailure();
    }

    @Override
    public void flagOneSuccess() {
        merger.mergeSuccess();
    }

    @Override
    public void add(ArbitrateListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void start() {
        merger.ge(minSuccessNum).exceptionally((e) -> {
            logger.error("replicate data:{} caught exception:{}", replicateData, e);
            return false;
        }).thenAccept((success)->{
            if (success){
                doSuccessListen();
                return;
            }

            doFailListen();
        });
    }

    private void doSuccessListen() {
        for (ArbitrateListener listener : listeners) {
            listener.onSuccess();
        }
    }

    private void doFailListen() {
        for (ArbitrateListener listener : listeners) {
            listener.onFail();
        }
    }
}

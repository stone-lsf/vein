package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.Merger;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午2:00
 */
public class MinReplicateArbitrator extends LogSupport implements ReplicateArbitrator {

    private CopyOnWriteArrayList<ArbitrateListener> listeners = new CopyOnWriteArrayList<>();
    private final int minSuccessNum;
    private final Merger merger;
    private final int timeout;

    public MinReplicateArbitrator(int minSuccessNum, int replicateCount, int timeout) {
        this.minSuccessNum = minSuccessNum;
        this.merger = new Merger(replicateCount);
        this.timeout = timeout;
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
    public void run() {
        boolean success;
        try {
            success = merger.ge(minSuccessNum, timeout);
        } catch (Exception e) {
            logger.error("waiting replicate arbitrator caught exception", e);
            doFailListen();
            return;
        }

        if (success) {
            doSuccessListen();
            return;
        }
        doFailListen();
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

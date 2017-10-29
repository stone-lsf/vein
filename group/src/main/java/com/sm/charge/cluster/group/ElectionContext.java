package com.sm.charge.cluster.group;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午9:17
 */
public abstract class ElectionContext implements ElectionCallback {

    private final ElectionCallback callback;
    private final int requiredJoins;
    private final AtomicBoolean enoughJoins = new AtomicBoolean(false);

    public ElectionContext(ElectionCallback callback, int requiredJoins) {
        this.callback = callback;
        this.requiredJoins = requiredJoins;
    }

    abstract void onClose();

    @Override
    public void onElectAsLeader() {

    }

    @Override
    public void onFailure(Throwable error) {

    }

    public ElectionCallback getCallback() {
        return callback;
    }

    public int getRequiredJoins() {
        return requiredJoins;
    }

    public AtomicBoolean getEnoughJoins() {
        return enoughJoins;
    }
}

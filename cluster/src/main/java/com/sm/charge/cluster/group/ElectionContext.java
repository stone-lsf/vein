package com.sm.charge.cluster.group;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午9:17
 */
public abstract class ElectionContext implements ElectionCallback {

    private final ElectionCallback callback;
    private final int requiredJoins;
    private boolean closed = false;

    public ElectionContext(ElectionCallback callback, int requiredJoins) {
        this.callback = callback;
        this.requiredJoins = requiredJoins;
    }

    abstract void onClose();

    @Override
    public synchronized void onElectAsLeader() {
        if (!closed) {
            try {
                callback.onElectAsLeader();
            } finally {
                closed = true;
                onClose();
            }
        }
    }

    @Override
    public synchronized void onFailure(Throwable error) {
        if (!closed) {
            try {
                callback.onFailure(error);
            } finally {
                closed = true;
                onClose();
            }
        }
    }

    public int getRequiredJoins() {
        return requiredJoins;
    }
}

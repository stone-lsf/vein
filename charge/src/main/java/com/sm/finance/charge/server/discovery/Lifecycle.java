package com.sm.finance.charge.server.discovery;

/**
 * @author shifeng.luo
 * @version created on 2017/9/2 下午1:34
 */
public class Lifecycle {
    private volatile LifeState state = LifeState.init;

    private final LifecycleComponent component;

    public Lifecycle(LifecycleComponent component) {
        this.component = component;
    }

    public LifeState state() {
        return state;
    }

    public boolean initailized() {
        return this.state() == LifeState.init;
    }

    public boolean starting() {
        return this.state() == LifeState.starting;
    }

    public boolean started() {
        return this.state() == LifeState.started;
    }

    public boolean stopping() {
        return this.state() == LifeState.stopping;
    }

    public boolean stopped() {
        return this.state() == LifeState.stopped;
    }

    public boolean closing() {
        return this.state() == LifeState.closing;
    }

    public boolean closed() {
        return this.state() == LifeState.closed;
    }

    public synchronized void onStart() {
        this.state = state.onStart(component);
    }

    public synchronized void onStartComplete() {
        this.state = state.onStartComplete(component);
    }


    public synchronized void onStop() {
        this.state = state.onStop(component);
    }

    public synchronized void onStopComplete() {
        this.state = state.onStopComplete(component);
    }

    public synchronized void onClose() {
        this.state = state.onClose(component);
    }

    public synchronized void onCloseComplete() {
        this.state = state.onCloseComplete(component);
    }
}

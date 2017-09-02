package com.sm.finance.charge.server.discovery;

import com.sm.finance.charge.server.common.Closable;
import com.sm.finance.charge.server.common.Startable;
import com.sm.finance.charge.server.common.Stoppable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author shifeng.luo
 * @version created on 2017/9/2 下午1:42
 */
public abstract class AbstractLifecycleComponent implements LifecycleComponent,Startable, Stoppable, Closable {

    protected final Lifecycle lifecycle;

    public AbstractLifecycleComponent() {
        lifecycle = new Lifecycle(this);
    }

    private final List<LifecycleListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public LifeState state() {
        return lifecycle.state();
    }

    @Override
    public void add(LifecycleListener listener) {
        listeners.add(listener);
    }

    @Override
    public void remove(LifecycleListener listener) {
        listeners.remove(listener);
    }


    @Override
    public void start() throws Exception {
        lifecycle.onStart();
    }

    @Override
    public void onStarting() {
        for (LifecycleListener listener : listeners) {
            listener.onStarting();
        }

        doStart();
        lifecycle.onStartComplete();
    }

    protected abstract void doStart();

    @Override
    public void onStarted() {
        for (LifecycleListener listener : listeners) {
            listener.onStarted();
        }
    }

    @Override
    public void stop() throws Exception {
        lifecycle.onStop();
    }

    @Override
    public void onStopping() {
        for (LifecycleListener listener : listeners) {
            listener.onStopping();
        }

        doStop();
        lifecycle.onStopComplete();
    }

    protected abstract void doStop();

    @Override
    public void onStopped() {
        for (LifecycleListener listener : listeners) {
            listener.onStopped();
        }
    }

    @Override
    public void close() throws Exception {
        lifecycle.onClose();
    }

    @Override
    public void onClosing() {
        for (LifecycleListener listener : listeners) {
            listener.onClosing();
        }

        doClose();
        lifecycle.onCloseComplete();
    }

    protected abstract void doClose();

    @Override
    public void onClosed() {
        for (LifecycleListener listener : listeners) {
            listener.onClosed();
        }
    }
}

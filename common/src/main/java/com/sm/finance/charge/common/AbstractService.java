package com.sm.finance.charge.common;

import com.sm.finance.charge.common.exceptions.NotStartedException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:45
 */
public abstract class AbstractService extends LogSupport implements Startable, AutoCloseable {

    protected AtomicBoolean started = new AtomicBoolean(false);
    protected AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            doStart();
        }
    }

    protected abstract void doStart() throws Exception;

    @Override
    public void close() throws Exception {
        if (closed.compareAndSet(false, true)) {
            doClose();
        }
    }

    protected abstract void doClose();

    protected void checkStarted() {
        if (!started.get()) {
            throw new NotStartedException("SequentialStore hasn't started!");
        }
    }
}

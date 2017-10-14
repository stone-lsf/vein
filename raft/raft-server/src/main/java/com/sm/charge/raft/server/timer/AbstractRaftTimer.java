package com.sm.charge.raft.server.timer;

import com.sm.finance.charge.common.LogSupport;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午4:08
 */
public abstract class AbstractRaftTimer extends LogSupport implements RaftTimer {


    private final ScheduledExecutorService executor;

    private volatile ScheduledFuture<?> future;

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(true);

    public AbstractRaftTimer(ScheduledExecutorService executor) {
        this.executor = executor;
    }


    @Override
    public void reset() {
        if (!started.get()) {
            throw new IllegalStateException("定时器并未启动!");
        }

        if (future != null) {
            future.cancel(false);
            future = executor.schedule(this, interval(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            future = executor.schedule(this, interval(), TimeUnit.MILLISECONDS);
            stopped.set(false);
        }
    }

    @Override
    public void stop() {
        if (stopped.compareAndSet(false, true)) {
            future.cancel(false);
            future = null;
            started.set(false);
        }
    }
}

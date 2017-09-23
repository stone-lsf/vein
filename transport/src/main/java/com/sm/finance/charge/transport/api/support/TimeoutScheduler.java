package com.sm.finance.charge.transport.api.support;

import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.Response;
import com.sm.finance.charge.transport.api.exceptions.TimeoutException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author shifeng.luo
 * @version created on 2017/9/23 下午3:51
 */
public class TimeoutScheduler {
    private static final int PROCESSORS;
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR;
    private final Connection connection;
    private ConcurrentMap<Integer, ScheduledFuture<?>> scheduledMap = new ConcurrentHashMap<>();

    static {
        PROCESSORS = Runtime.getRuntime().availableProcessors();
        SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(PROCESSORS + 1, new NamedThreadFactory("TimeoutHandler"));
    }

    public TimeoutScheduler(Connection connection) {
        this.connection = connection;
    }

    public void schedule(int requestId, int timeout) {
        ScheduledFuture<?> future = SCHEDULED_EXECUTOR.schedule(new TimeoutTask(requestId, timeout), timeout, TimeUnit.MILLISECONDS);
        scheduledMap.put(requestId, future);
    }


    public void cancel(int requestId) {
        ScheduledFuture<?> future = scheduledMap.remove(requestId);
        if (future != null) {
            future.cancel(false);
        }
    }


    private class TimeoutTask implements Runnable {
        private final int requestId;
        private final int timeout;

        private TimeoutTask(int requestId, int timeout) {
            this.requestId = requestId;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            TimeoutException exception = new TimeoutException(timeout);
            Response response = new Response(requestId);
            response.setException(exception);
            connection.onMessage(response);
        }
    }
}

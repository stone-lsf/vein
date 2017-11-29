package com.vein.transport.api.handler;

import com.vein.common.base.LoggerSupport;
import com.vein.common.NamedThreadFactory;
import com.vein.transport.api.Connection;
import com.vein.transport.api.Response;
import com.vein.transport.api.exceptions.TimeoutException;
import com.vein.transport.api.support.ResponseContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.vein.common.SystemConstants.PROCESSORS;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:54
 */
public class TimeoutResponseHandler<T> extends LoggerSupport implements ResponseHandler<T> {

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR;
    private final ResponseHandler<T> handler;
    private final Connection connection;
    private final int requestId;
    private final int timeout;

    private volatile ScheduledFuture<?> schedule;

    static {
        SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(PROCESSORS + 1, new NamedThreadFactory("TimeoutHandler"));
    }

    public TimeoutResponseHandler(ResponseHandler<T> handler, Connection connection, int requestId, int timeout) {
        this.handler = handler;
        this.connection = connection;
        this.requestId = requestId;
        this.timeout = timeout;
        this.schedule = SCHEDULED_EXECUTOR.schedule(newTimeoutTask(), timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handle(T response, Connection connection) {
        schedule.cancel(false);
        handler.handle(response, connection);
    }

    @Override
    public void onException(Throwable e, ResponseContext context) {
        schedule.cancel(false);
        handler.onException(e, context);
    }

    private Runnable newTimeoutTask() {
        return () -> {
            TimeoutException exception = new TimeoutException(timeout);
            Response response = new Response(requestId);
            response.setException(exception);
            connection.onMessage(response);
        };
    }
}

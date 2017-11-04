package com.sm.charge.raft.server;

import com.sm.charge.raft.server.state.Event;
import com.sm.charge.raft.server.state.EventExecutor;
import com.sm.finance.charge.common.base.LoggerSupport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author shifeng.luo
 * @version created on 2017/11/4 下午6:11
 */
public class RaftEventExecutor extends LoggerSupport implements EventExecutor {

    private final ExecutorService executor;
    private ConcurrentMap<Class<? extends Event>, Function> handlers = new ConcurrentHashMap<>();

    public RaftEventExecutor(ExecutorService executor) {
        this.executor = executor;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> submit(Event event) {
        Class<? extends Event> eventClass = event.getClass();
        Function<Event, T> handler = handlers.get(eventClass);
        if (handler == null) {
            logger.error("event:{} don't have handler", eventClass);
            throw new IllegalStateException("event don't have handler");
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                T response = handler.apply(event);
                future.complete(response);
            } catch (Throwable e) {
                logger.error("handle event:{} caught exception", event, e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> execute(Event event) {
        return submit(event);
    }

    @Override
    public <T extends Event> void register(Class<T> event, Consumer<T> handler) {
        Function<T, Void> function = t -> {
            handler.accept(t);
            return null;
        };

        handlers.put(event, function);
    }

    @Override
    public <T extends Event, R> void register(Class<T> event, Function<T, R> handler) {
        handlers.put(event, handler);
    }
}

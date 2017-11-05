package com.sm.charge.raft.server.events;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 上午12:14
 */
public interface EventExecutor {

    <T> CompletableFuture<T> submit(Event event);

    CompletableFuture<Void>  execute(Event event);

    <T extends Event> void register(Class<T> event, Consumer<T> consumer);

    <T extends Event, R> void register(Class<T> event, Function<T, R> consumer);
}

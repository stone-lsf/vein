package com.vein.raft.client;

import com.vein.serializer.api.Serializer;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:39
 */
public interface RaftClient {

    void start();

    Serializer serializer();

    <T> CompletableFuture<T> submit(Command command);

    void register(Watcher watcher);

    void close();
}

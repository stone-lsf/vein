package com.sm.charge.raft.server;

import com.sm.charge.raft.client.Command;
import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.Startable;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:45
 */
public interface RaftServer extends Startable, Closable {

    long getId();

    /**
     * 加入集群
     *
     * @return 成功则返回true，否则返回false
     */
    CompletableFuture<Boolean> join();

    CompletableFuture<Boolean> leave();

    CompletableFuture<Object> handle(Command command);
}

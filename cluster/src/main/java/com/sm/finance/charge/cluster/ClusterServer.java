package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.client.Command;
import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:45
 */
public interface ClusterServer extends Startable, Closable {

    /**
     * 加入集群
     *
     * @return 成功则返回true，否则返回false
     */
    CompletableFuture<Boolean> join();

    CompletableFuture<Boolean> leave();

    CompletableFuture<Object> handle(Command command);
}

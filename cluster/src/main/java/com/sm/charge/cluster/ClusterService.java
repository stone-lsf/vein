package com.sm.charge.cluster;

import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午4:46
 */
public interface ClusterService extends Startable, Closable {

    void createGroup(String groupName, List<ServerInfo> servers);


    CompletableFuture<Boolean> receive(ClusterMessage message);
}

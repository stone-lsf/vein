package com.sm.charge.cluster.group;

import com.sm.charge.cluster.ClusterMessage;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午10:39
 */
public interface GroupService {

    CompletableFuture<Boolean> receive(ClusterMessage message);
}

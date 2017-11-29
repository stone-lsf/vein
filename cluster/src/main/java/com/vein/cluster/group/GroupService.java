package com.vein.cluster.group;

import com.vein.cluster.ClusterMessage;
import com.vein.common.base.Startable;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午10:39
 */
public interface GroupService extends Startable{

    CompletableFuture<Boolean> receive(ClusterMessage message);
}

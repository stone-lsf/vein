package com.vein.cluster;

import com.vein.common.base.Closable;
import com.vein.common.base.Startable;
import com.vein.discovery.DiscoveryServer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午4:46
 */
public interface ClusterService extends Startable, Closable {

    void createGroup(String groupName, List<ServerInfo> servers);


    CompletableFuture<Boolean> receive(ClusterMessage message);


    DiscoveryServer getDiscoveryServer();
}

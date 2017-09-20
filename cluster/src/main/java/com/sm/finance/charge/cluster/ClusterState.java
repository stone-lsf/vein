package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:23
 */
public class ClusterState {

    private final DiscoveryNodes nodes;
    /**
     * 集群自增版本号，只有master才能修改
     */
    private volatile long version;

    public ClusterState(DiscoveryNodes nodes) {
        this.nodes = nodes;
    }

    public DiscoveryNodes getNodes() {
        return nodes;
    }
}

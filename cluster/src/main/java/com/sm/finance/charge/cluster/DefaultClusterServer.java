package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.discovery.DefaultDiscoveryService;
import com.sm.finance.charge.cluster.discovery.DiscoveryConfig;
import com.sm.finance.charge.cluster.discovery.DiscoveryService;
import com.sm.finance.charge.common.AbstractService;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:14
 */
public class DefaultClusterServer extends AbstractService implements ClusterServer {

    private DiscoveryService discoveryService;
    private String clusterName;

    public DefaultClusterServer() {
        DiscoveryConfig discoveryConfig = new DiscoveryConfig();
        discoveryService = new DefaultDiscoveryService(discoveryConfig);
    }

    @Override
    public boolean join() {
        return discoveryService.join(clusterName);
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {

    }
}

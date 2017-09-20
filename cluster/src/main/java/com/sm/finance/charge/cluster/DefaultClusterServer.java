package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.discovery.DefaultDiscoveryService;
import com.sm.finance.charge.cluster.discovery.DiscoveryConfig;
import com.sm.finance.charge.cluster.discovery.DiscoveryService;
import com.sm.finance.charge.cluster.replicate.MajorReplicateArbitrator;
import com.sm.finance.charge.cluster.replicate.ReplicateData;
import com.sm.finance.charge.common.AbstractService;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:14
 */
public class DefaultClusterServer extends AbstractService implements ClusterServer {

    private DiscoveryService discoveryService;
    private String clusterName;
    private ClusterConfig clusterConfig;

    public DefaultClusterServer() {
        DiscoveryConfig discoveryConfig = new DiscoveryConfig();
        discoveryService = new DefaultDiscoveryService(discoveryConfig);
    }

    @Override
    public boolean join() {
        return discoveryService.join(clusterName);
    }

    @Override
    public void send(Object message) {
        ReplicateData data = new ReplicateData();
        data.setPayload(message);

        MajorReplicateArbitrator arbitrator = new MajorReplicateArbitrator(clusterConfig.getCandidateCount(), data);
        data.setNotifier(arbitrator::start);

        //TODO 设置arbitrator监听器

        discoveryService.deliver(data);
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {

    }
}

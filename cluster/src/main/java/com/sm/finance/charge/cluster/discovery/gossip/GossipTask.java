package com.sm.finance.charge.cluster.discovery.gossip;

import com.sm.finance.charge.cluster.discovery.DiscoveryNode;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;
import com.sm.finance.charge.cluster.discovery.NodeFilter;
import com.sm.finance.charge.common.LogSupport;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:10
 */
public class GossipTask extends LogSupport implements Runnable {

    private final DiscoveryNodes nodes;
    private final GossipMessageService messageService;
    private final NodeFilter filter = new GossipTask.Filter();


    public GossipTask(DiscoveryNodes nodes, GossipMessageService messageService) {
        this.nodes = nodes;
        this.messageService = messageService;
    }


    @Override
    public void run() {

    }

    private class Filter implements NodeFilter {

        @Override
        public boolean apply(DiscoveryNode node) {
            return nodes.isLocalNode(node.getNodeId());
        }
    }
}

package com.sm.finance.charge.cluster.discovery.gossip;

import com.sm.finance.charge.cluster.discovery.DiscoveryConfig;
import com.sm.finance.charge.cluster.discovery.DiscoveryNode;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;
import com.sm.finance.charge.cluster.discovery.NodeFilter;
import com.sm.finance.charge.cluster.discovery.gossip.messages.GossipMessage;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.transport.api.Connection;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:10
 */
public class GossipTask extends LogSupport implements Runnable {

    private final DiscoveryNodes nodes;
    private final MessageQueue messageQueue;
    private final NodeFilter filter = new GossipTask.Filter();
    private final int gossipNodes;
    private final int maxGossipCount;


    public GossipTask(DiscoveryNodes nodes, MessageQueue messageQueue, DiscoveryConfig config){
        this.nodes = nodes;
        this.messageQueue = messageQueue;
        this.gossipNodes = config.getNodesPerGossip();
        this.maxGossipCount = config.getMaxGossipMessageCount();
    }


    @Override
    public void run() {
        List<DiscoveryNode> randomNodes = nodes.randomNodes(gossipNodes, filter);
        if (CollectionUtils.isEmpty(randomNodes)) {
            return;
        }

        List<GossipMessage> messages = messageQueue.dequeue(maxGossipCount);

        for (DiscoveryNode node : randomNodes) {
            Connection connection = node.getConnection();
            if (connection == null) {
                logger.error("node:{} don't have connection", node.getNodeId());
                throw new IllegalStateException("node:" + node.getNodeId() + " don't have connection");
            }

            try {
                GossipRequest request = new GossipRequest(messages);
                connection.send(request);
            } catch (Exception e) {
                logger.error("gossip message to node:{} caught exception:{}", node.getNodeId(), e);
            }
        }
    }

    private class Filter implements NodeFilter {

        @Override
        public boolean apply(DiscoveryNode node) {
            return nodes.isLocalNode(node.getNodeId());
        }
    }
}

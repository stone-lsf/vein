package com.sm.charge.memory.gossip;

import com.sm.charge.memory.DiscoveryConfig;
import com.sm.charge.memory.Node;
import com.sm.charge.memory.NodeFilter;
import com.sm.charge.memory.Nodes;
import com.sm.charge.memory.gossip.messages.GossipContent;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:10
 */
public class GossipTask extends LoggerSupport implements Runnable {

    private final Nodes nodes;
    private final MessageGossiper messageQueue;
    private final NodeFilter filter = new GossipTask.Filter();
    private final int gossipNodes;
    private final int maxGossipCount;


    public GossipTask(Nodes nodes, MessageGossiper messageQueue, DiscoveryConfig config) {
        this.nodes = nodes;
        this.messageQueue = messageQueue;
        this.gossipNodes = config.getGossipNodes();
        this.maxGossipCount = config.getMaxGossipMessageCount();
    }


    @Override
    public void run() {
        List<Node> randomNodes = nodes.randomNodes(gossipNodes, filter);
        if (CollectionUtils.isEmpty(randomNodes)) {
            logger.info("node:{} can't find target to gossip", nodes.getSelf());
            return;
        }

        List<GossipContent> messages = messageQueue.dequeue(maxGossipCount);
        logger.info("node:{} trying to gossip {} messages", nodes.getSelf(), messages.size());
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }


        for (Node node : randomNodes) {
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
        public boolean apply(Node node) {
            return nodes.isLocalNode(node.getNodeId());
        }
    }
}

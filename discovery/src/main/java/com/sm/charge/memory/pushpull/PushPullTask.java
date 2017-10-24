package com.sm.charge.memory.pushpull;

import com.sm.charge.memory.DiscoveryNode;
import com.sm.charge.memory.DiscoveryNodes;
import com.sm.charge.memory.NodeFilter;
import com.sm.finance.charge.common.base.LoggerSupport;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:10
 */
public class PushPullTask extends LoggerSupport implements Runnable {

    private final DiscoveryNodes nodes;
    private final PushPullService pushPullService;
    private final NodeFilter filter = new Filter();

    public PushPullTask(DiscoveryNodes nodes, PushPullService pushPullService) {
        this.nodes = nodes;
        this.pushPullService = pushPullService;
    }

    @Override
    public void run() {
        List<DiscoveryNode> randomNodes = nodes.randomNodes(1, filter);
        if (CollectionUtils.isEmpty(randomNodes)) {
            return;
        }

        DiscoveryNode node = randomNodes.get(0);
        logger.info("start push pull from node:{}", node.getNodeId());
        try {
            pushPullService.pushPull(node.getAddress());
        } catch (Exception e) {
            logger.error("push pull from node:{} caught exception:{}", node.getNodeId(), e);
        }
    }


    private class Filter implements NodeFilter {

        @Override
        public boolean apply(DiscoveryNode node) {
            return nodes.isLocalNode(node.getNodeId()) || node.getStatus() != DiscoveryNode.Status.ALIVE;
        }
    }
}

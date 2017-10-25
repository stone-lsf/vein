package com.sm.charge.memory.pushpull;

import com.sm.charge.memory.Node;
import com.sm.charge.memory.NodeFilter;
import com.sm.charge.memory.Nodes;
import com.sm.finance.charge.common.base.LoggerSupport;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.sm.charge.memory.NodeStatus.ALIVE;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:10
 */
public class PushPullTask extends LoggerSupport implements Runnable {

    private final Nodes nodes;
    private final PushPullService pushPullService;
    private final NodeFilter filter = new Filter();

    public PushPullTask(Nodes nodes, PushPullService pushPullService) {
        this.nodes = nodes;
        this.pushPullService = pushPullService;
    }

    @Override
    public void run() {
        List<Node> randomNodes = nodes.randomNodes(1, filter);
        if (CollectionUtils.isEmpty(randomNodes)) {
            return;
        }

        Node node = randomNodes.get(0);
        logger.info("start push pull from node:{}", node.getNodeId());
        try {
            pushPullService.pushPull(node.getAddress());
        } catch (Exception e) {
            logger.error("push pull from node:{} caught exception:{}", node.getNodeId(), e);
        }
    }


    private class Filter implements NodeFilter {

        @Override
        public boolean apply(Node node) {
            return nodes.isLocalNode(node.getNodeId()) || node.getStatus() != ALIVE;
        }
    }
}

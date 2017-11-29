package com.vein.discovery.pushpull;

import com.vein.discovery.Node;
import com.vein.discovery.NodeFilter;
import com.vein.discovery.Nodes;
import com.vein.common.base.LoggerSupport;
import com.vein.discovery.NodeStatus;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

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
        String nodeId = null;
        try {
            List<Node> randomNodes = nodes.randomNodes(1, filter);
            if (CollectionUtils.isEmpty(randomNodes)) {
                return;
            }

            Node node = randomNodes.get(0);
            nodeId = node.getNodeId();
            logger.info("start push pull from node:{}", nodeId);

            pushPullService.pushPull(node.getAddress());
        } catch (Throwable e) {
            logger.error("push pull from node:{} caught exception:{}", nodeId, e);
        }
    }


    private class Filter implements NodeFilter {

        @Override
        public boolean apply(Node node) {
            return nodes.isLocalNode(node.getNodeId()) || node.getStatus() != NodeStatus.ALIVE;
        }
    }
}

package com.sm.charge.memory.probe;

import com.sm.charge.memory.DiscoveryConfig;
import com.sm.charge.memory.Node;
import com.sm.charge.memory.Nodes;
import com.sm.charge.memory.gossip.GossipMessageService;
import com.sm.charge.memory.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.base.LoggerSupport;

import static com.sm.charge.memory.NodeStatus.DEAD;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:10
 */
public class ProbeTask extends LoggerSupport implements Runnable {
    private final Nodes nodes;
    private final ProbeService probeService;
    private volatile int probeIndex = 0;
    private final int pingTimeout;
    private final int redirectPingTimeout;
    private final GossipMessageService messageService;

    public ProbeTask(Nodes nodes, ProbeService probeService, DiscoveryConfig config, GossipMessageService messageService) {
        this.nodes = nodes;
        this.probeService = probeService;
        this.pingTimeout = config.getPingTimeout();
        this.redirectPingTimeout = config.getRedirectPingTimeout();
        this.messageService = messageService;
    }


    @Override
    public void run() {
        int numCheck = 0;

        try {
            while (true) {
                int size = nodes.size();
                if (numCheck >= size) {
                    logger.info("node:{} can't find suitable node to probe，checked:{},total:{}", nodes.getSelf(), numCheck, size);
                    return;
                }

                if (probeIndex >= size) {
                    probeIndex = 0;
                    nodes.removeDeadNodes();
                    continue;
                }

                Node node = nodes.get(probeIndex);
                numCheck++;
                probeIndex++;

                if (node == null || node.getStatus() == DEAD || nodes.isLocalNode(node.getNodeId())) {
                    continue;
                }

                probe(node);
                return;
            }
        } catch (Throwable e) {
            logger.error("execute throwable task caught exception", e);
        }
    }


    private void probe(Node node) {
        Ack ack = probeService.ping(node, pingTimeout);
        if (ack != null) {
            return;
        }

        boolean success = probeService.redirectPing(node, redirectPingTimeout);
        if (success) {
            return;
        }
        logger.warn("node:{} can't success probe node:{} ", nodes.getLocalNode().getNodeId(), node.getNodeId());

        long incarnation = node.getIncarnation();
        SuspectMessage message = new SuspectMessage(node.getNodeId(), node.getAddress(), incarnation, nodes.getSelf());
        messageService.suspectNode(message);
    }
}

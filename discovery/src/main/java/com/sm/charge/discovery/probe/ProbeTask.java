package com.sm.charge.discovery.probe;

import com.sm.charge.discovery.DiscoveryConfig;
import com.sm.charge.discovery.Node;
import com.sm.charge.discovery.Nodes;
import com.sm.charge.discovery.gossip.GossipMessageService;
import com.sm.charge.discovery.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.base.LoggerSupport;

import static com.sm.charge.discovery.NodeStatus.DEAD;

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
                logger.info("nodes size:{},probeIndex:{}", size, probeIndex);
                if (probeIndex >= size) {
                    probeIndex = 0;
                    nodes.removeDeadNodes();
                    continue;
                }

                if (numCheck >= size) {
                    logger.info("node:{} can't find suitable node to probe，checked:{},total:{}", nodes.getSelf(), numCheck, size);
                    return;
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

        logger.info("ping node:{} failed, starting to redirect ping", node.getNodeId());
        boolean success = probeService.redirectPing(node, redirectPingTimeout);
        if (success) {
            return;
        }
        logger.warn("redirect probe node:{} failed! ", node.getNodeId());

        long incarnation = node.getIncarnation();
        SuspectMessage message = new SuspectMessage(node.getNodeId(), node.getAddress(), incarnation, nodes.getSelf());
        messageService.suspectNode(message);
    }
}

package com.sm.finance.charge.cluster.discovery.probe;

import com.sm.finance.charge.cluster.discovery.DiscoveryConfig;
import com.sm.finance.charge.cluster.discovery.DiscoveryNode;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;
import com.sm.finance.charge.cluster.discovery.gossip.GossipMessageService;
import com.sm.finance.charge.cluster.discovery.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.LogSupport;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:10
 */
public class ProbeTask extends LogSupport implements Runnable {
    private final DiscoveryNodes nodes;
    private final ProbeService probeService;
    private volatile int probeIndex = 0;
    private final int pingTimeout;
    private final int redirectPingTimeout;
    private final GossipMessageService messageService;

    public ProbeTask(DiscoveryNodes nodes, ProbeService probeService, DiscoveryConfig config, GossipMessageService messageService) {
        this.nodes = nodes;
        this.probeService = probeService;
        this.pingTimeout = config.getPingTimeout();
        this.redirectPingTimeout = config.getRedirectPingTimeout();
        this.messageService = messageService;
    }


    @Override
    public void run() {
        int numCheck = 0;

        while (true) {
            int size = nodes.size();
            if (numCheck >= size) {
                logger.info("can't find suitable node to probe");
                return;
            }

            if (probeIndex >= size) {
                probeIndex = 0;
                continue;
            }

            DiscoveryNode node = nodes.get(probeIndex);
            numCheck++;
            if (node == null) {
                continue;
            }
            probeIndex++;

            if (nodes.isLocalNode(node.getNodeId())) {
                continue;
            }

            probe(node);
            return;
        }
    }


    private void probe(DiscoveryNode node) {
        Ack ack = probeService.ping(node, pingTimeout);
        if (ack != null) {
            return;
        }

        boolean success = probeService.redirectPing(node, redirectPingTimeout);
        if (success) {
            return;
        }

        long incarnation = node.getState().getIncarnation();
        SuspectMessage message = new SuspectMessage(node.getNodeId(), node.getAddress(), incarnation, nodes.getLocalNodeId());
        messageService.suspectNode(message);
    }
}

package com.sm.charge.memory.gossip;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import com.sm.charge.memory.DiscoveryConfig;
import com.sm.charge.memory.Node;
import com.sm.charge.memory.NodeFilter;
import com.sm.charge.memory.Nodes;
import com.sm.charge.memory.gossip.messages.GossipContent;
import com.sm.charge.memory.gossip.messages.GossipMessage;
import com.sm.charge.memory.gossip.messages.MemberMessage;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;

import org.apache.commons.collections.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/9/19 下午1:02
 */
public class MessageGossiper extends LoggerSupport {

    private ListMultimap<String, MemberMessage> nodeMessages = ArrayListMultimap.create();

    private BlockingQueue<GossipMessage> messages;

    private AtomicBoolean gossiping = new AtomicBoolean(false);

    private final Nodes nodes;
    private final NodeFilter filter = new Filter();
    private final int gossipNodes;
    private final int maxGossipCount;
    private final int maxTransmit;
    private final ScheduledExecutorService executorService;


    public MessageGossiper(Nodes nodes, DiscoveryConfig config, ScheduledExecutorService executorService) {
        this.messages = new LinkedBlockingQueue<>(config.getGossipQueueSize());
        this.nodes = nodes;
        this.gossipNodes = config.getGossipNodes();
        this.maxGossipCount = config.getMaxGossipMessageCount();
        this.executorService = executorService;
        this.maxTransmit = config.getMaxGossipTimes();
    }

    public boolean gossip(MemberMessage message) {
        try {
            synchronized (this) {
                List<MemberMessage> memberMessages = nodeMessages.get(message.getContent().getNodeId());
                if (CollectionUtils.isNotEmpty(memberMessages)) {
                    for (MemberMessage oldMessage : memberMessages) {
                        message.invalidate(oldMessage);
                    }
                }
            }
            messages.put(message);
            wakeUp();
            return true;
        } catch (InterruptedException e) {
            logger.error("enqueue com.sm.charge.memory.gossip message:{} caught exception:{}", message, e);
            return false;
        }
    }

    private void wakeUp() {
        if (gossiping.compareAndSet(false, true)) {
            executorService.submit(this::doGossip);
        }
    }

    private void doGossip() {
        while (true) {
            List<Node> randomNodes = nodes.randomNodes(gossipNodes, filter);
            if (CollectionUtils.isEmpty(randomNodes)) {
                logger.info("node:{} can't find target to gossip", nodes.getSelf());
                break;
            }

            List<GossipContent> contents = this.getMessage(maxGossipCount);
            logger.info("node:{} trying to gossip {} messages", nodes.getSelf(), contents.size());
            if (CollectionUtils.isEmpty(contents)) {
                break;
            }


            for (Node node : randomNodes) {
                Connection connection = node.getConnection();
                if (connection == null) {
                    logger.error("node:{} don't have connection", node.getNodeId());
                    continue;
                }

                try {
                    GossipRequest request = new GossipRequest(contents);
                    logger.info("gossip message:{} to node:{}", request, node.getNodeId());
                    connection.send(request);
                } catch (Exception e) {
                    logger.error("gossip message to node:{} caught exception:{}", node.getNodeId(), e);
                }
            }
        }
        gossiping.set(false);
    }


    private List<GossipContent> getMessage(int expectSize) {
        List<GossipContent> result = Lists.newArrayListWithCapacity(expectSize);

        Iterator<GossipMessage> iterator = messages.iterator();
        while (expectSize > 0 && iterator.hasNext()) {
            GossipMessage message = iterator.next();
            if (message.invalid()) {
                iterator.remove();
                continue;
            }

            if (message.transmits() > maxTransmit) {
                message.onGossipFinish();
                iterator.remove();
                continue;
            }
            message.increaseTransmits();
            result.add(message.getContent());
            expectSize--;
        }

        return result;
    }


    private class Filter implements NodeFilter {

        @Override
        public boolean apply(Node node) {
            return nodes.isLocalNode(node.getNodeId());
        }
    }
}

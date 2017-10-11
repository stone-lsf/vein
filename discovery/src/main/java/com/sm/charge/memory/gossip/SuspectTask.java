package com.sm.charge.memory.gossip;

import com.sm.charge.memory.DiscoveryNode;
import com.sm.charge.memory.DiscoveryNodeState;
import com.sm.charge.memory.DiscoveryNodes;
import com.sm.charge.memory.gossip.messages.DeadMessage;
import com.sm.finance.charge.common.LogSupport;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/19 下午1:22
 */
public class SuspectTask extends LogSupport implements Runnable {

    /**
     * 被猜疑的节点
     */
    private final String nodeId;

    private final DiscoveryNodes nodes;

    private final GossipMessageService messageService;

    private final Date createTime;

    private volatile ScheduledFuture future;

    public SuspectTask(String nodeId, DiscoveryNodes nodes, GossipMessageService messageService, Date createTime) {
        this.nodeId = nodeId;
        this.nodes = nodes;
        this.messageService = messageService;
        this.createTime = createTime;
    }


    @Override
    public void run() {
        logger.info("handle node[{}] suspect time out", nodeId);
        DiscoveryNode node = nodes.get(nodeId);
        if (node == null) {
            logger.warn("handle node:{} suspect timeout,but it's not in nodes", nodeId);
            return;
        }

        DiscoveryNodeState state = node.getState();
        DiscoveryNode.Status status = state.getStatus();
        if (status == DiscoveryNode.Status.SUSPECT && state.getStatusChangeTime().equals(createTime)) {
            logger.info("marking node [{}] as failed by suspect timeout happened", nodeId);
            DeadMessage message = new DeadMessage(nodeId, state.getIncarnation(), nodes.getLocalNodeId());
            messageService.deadNode(message);
        }
    }

    public void cancel() {
        if (future != null) {
            future.cancel(false);
        }
    }

    public String getNodeId() {
        return nodeId;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public ScheduledFuture getFuture() {
        return future;
    }

    public void setFuture(ScheduledFuture future) {
        this.future = future;
    }
}

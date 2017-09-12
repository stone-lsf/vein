package com.sm.finance.charge.cluster.discovery.gossip;

import com.sm.finance.charge.cluster.discovery.DiscoveryNode;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodeListener;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodeState;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;
import com.sm.finance.charge.cluster.discovery.gossip.messages.AliveMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.DeadMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.DeclareMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.transport.api.Connection;

import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.sm.finance.charge.cluster.discovery.DiscoveryNode.Status.ALIVE;
import static com.sm.finance.charge.cluster.discovery.DiscoveryNode.Status.DEAD;
import static com.sm.finance.charge.cluster.discovery.DiscoveryNode.Status.SUSPECT;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:23
 */
public class GossipMessageController extends LogSupport implements GossipMessageService {

    private final CopyOnWriteArrayList<DiscoveryNodeListener> listeners = new CopyOnWriteArrayList<>();
    private final DiscoveryNodes nodes;

    public GossipMessageController(DiscoveryNodes nodes) {
        this.nodes = nodes;
    }

    @Override
    public void aliveNode(AliveMessage message, boolean bootstrap) {
        String toAliveNode = message.getNodeId();

        DiscoveryNodeState state = new DiscoveryNodeState(toAliveNode, DEAD, new Date());
        DiscoveryNode node = new DiscoveryNode(message, state);
        DiscoveryNode existNode = nodes.addIfAbsent(node);
        if (existNode == null) {
            existNode = node;
        }

        existNode.lock();
        try {
            boolean isLocalNode = nodes.isLocalNode(toAliveNode);
            if (!isLocalNode && existNode.getConnection() == null) {
                Address address = message.getAddress();
                Connection connection = createConnection(address);
                if (connection == null) {
                    logger.warn("can't create connection to node address:{},ignore alive message", address);
                    return;
                }
                existNode.setConnection(connection);
            }

            long existIncarnation = existNode.getState().getIncarnation();
            long aliveIncarnation = message.getIncarnation();
            if (!isLocalNode && aliveIncarnation <= existIncarnation) {
                return;
            }

            if (isLocalNode && aliveIncarnation < existIncarnation) {
                return;
            }

            deleteSuspectTimer(toAliveNode);

            if (!bootstrap && isLocalNode) {
                if (aliveIncarnation == existIncarnation) {
                    return;
                }

                //当节点join或者被杀死重启时,本地incarnation可能比其它节点持有镜像的incarnation低
                refute(aliveIncarnation);
                logger.warn("refuting an alive message");
                return;
            }

            state = existNode.getState();
            DiscoveryNode.Status oldStatus = state.getStatus();
            if (oldStatus != ALIVE) {
                state.setIncarnation(aliveIncarnation);
                state.setStatus(ALIVE);
                state.setStatusChangeTime(new Date());
            }

            nodes.aliveNode(existNode);
            gossip(message);

            for (DiscoveryNodeListener listener : listeners) {
                if (oldStatus == DEAD) {
                    listener.onJoin(existNode);
                } else {
                    listener.onUpdate(existNode);
                }
            }
        } finally {
            existNode.unlock();
        }
    }

    private void deleteSuspectTimer(String nodeId) {

    }

    private void refute(long incarnation) {

    }

    private Connection createConnection(Address address) {

        return null;
    }

    private void gossip(DeclareMessage message) {

    }

    @Override
    public void suspectNode(SuspectMessage message) {
        String toSuspectNode = message.getNodeId();
        DiscoveryNode node = nodes.get(toSuspectNode);
        if (node == null) {
            logger.warn("receive suspect message of node:{},but it isn't in nodes", toSuspectNode);
            return;
        }

        node.lock();
        try {
            long suspectIncarnation = message.getIncarnation();
            DiscoveryNodeState state = node.getState();
            long existIncarnation = state.getIncarnation();

            if (suspectIncarnation < existIncarnation) {
                logger.warn("receive suspect message of node:{}, suspect incarnation:{} less than current:{}", toSuspectNode, suspectIncarnation, existIncarnation);
                return;
            }

            DiscoveryNode.Status oldStatus = state.getStatus();
            if (oldStatus != ALIVE) {
                logger.warn("receive suspect message of node:{}, current status is dead", oldStatus);
                return;
            }

            if (nodes.isLocalNode(toSuspectNode)) {
                logger.warn("refute a suspect message from {}", message.getFrom());
                refute(message.getIncarnation());
                return;
            }

            gossip(message);

            state.setIncarnation(suspectIncarnation);
            state.setStatus(SUSPECT);
            state.setStatusChangeTime(new Date());

            nodes.suspectNode(node);
            createSuspectTimer(toSuspectNode);
        } finally {
            node.unlock();
        }
    }


    private void createSuspectTimer(String nodeId) {

    }

    @Override
    public void deadNode(DeadMessage message) {
        String toDeadNode = message.getNodeId();
        DiscoveryNode node = nodes.get(toDeadNode);
        if (node == null) {
            logger.warn("receive dead message of node:{},but it isn't in nodes", toDeadNode);
            return;
        }

        node.lock();
        try {
            long deadIncarnation = message.getIncarnation();
            DiscoveryNodeState state = node.getState();
            long existIncarnation = state.getIncarnation();

            if (deadIncarnation < existIncarnation) {
                logger.warn("receive dead message of node:{}, dead incarnation:{} less than current:{}", toDeadNode, deadIncarnation, existIncarnation);
                return;
            }

            DiscoveryNode.Status oldStatus = state.getStatus();
            if (oldStatus == DEAD) {
                logger.warn("receive dead message of node:{}, current status is dead", toDeadNode);
                return;
            }

            if (nodes.isLocalNode(toDeadNode)) {
                logger.warn("refute a dead message from {}", message.getFrom());
                refute(deadIncarnation);
                return;
            }

            gossip(message);
            state.setStatus(DEAD);
            state.setIncarnation(deadIncarnation);
            state.setStatusChangeTime(new Date());

            nodes.deadNode(node);
            for (DiscoveryNodeListener listener : listeners) {
                listener.onLeave(node);
            }
        } finally {
            node.unlock();
        }
    }

    @Override
    public void addListener(DiscoveryNodeListener listener) {
        listeners.add(listener);
    }
}

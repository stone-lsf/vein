package com.sm.finance.charge.cluster.discovery.gossip;

import com.sm.finance.charge.cluster.discovery.DiscoveryNode;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodeListener;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodeState;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;
import com.sm.finance.charge.cluster.discovery.gossip.messages.AliveMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.DeadMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.GossipMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.exceptions.ConnectException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.sm.finance.charge.cluster.discovery.DiscoveryNode.Status.ALIVE;
import static com.sm.finance.charge.cluster.discovery.DiscoveryNode.Status.DEAD;
import static com.sm.finance.charge.cluster.discovery.DiscoveryNode.Status.SUSPECT;
import static com.sm.finance.charge.cluster.discovery.gossip.messages.GossipMessage.USER;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:23
 */
public class GossipMessageController extends LogSupport implements GossipMessageService {
    private final CopyOnWriteArrayList<DiscoveryNodeListener> listeners = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, SuspectTask> suspectTaskMap = new ConcurrentHashMap<>();

    private final DiscoveryNodes nodes;
    private final TransportClient client;
    private final MessageQueue messageQueue;
    private final int suspectTimeout;

    private final ScheduledExecutorService executorService;

    private GossipMessageNotifier messageNotifier;

    public GossipMessageController(DiscoveryNodes nodes, TransportClient client, MessageQueue messageQueue, int suspectTimeout, ScheduledExecutorService executorService) {
        this.nodes = nodes;
        this.client = client;
        this.messageQueue = messageQueue;
        this.suspectTimeout = suspectTimeout;
        this.executorService = executorService;
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

            deleteSuspectTask(toAliveNode);

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
            messageQueue.enqueue(message);

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

    private void deleteSuspectTask(String nodeId) {
        SuspectTask task = suspectTaskMap.remove(nodeId);
        if (task != null) {
            task.cancel();
        }
    }

    private void refute(long incarnation) {
        DiscoveryNode localNode = nodes.getLocalNode();
        DiscoveryNodeState state = localNode.getState();

        long newIncarnation = incarnation + 1;
        state.setIncarnation(newIncarnation);

        AliveMessage message = new AliveMessage(localNode.getNodeId(), localNode.getAddress(), newIncarnation, localNode.getType());
        messageQueue.enqueue(message);
    }

    private Connection createConnection(Address address) {
        try {
            return client.connect(address);
        } catch (ConnectException e) {
            logger.error("create connection to address:{} failure, caught exception:{}", address, e);
            return null;
        }
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

            messageQueue.enqueue(message);

            state.setIncarnation(suspectIncarnation);
            state.setStatus(SUSPECT);
            Date now = new Date();
            state.setStatusChangeTime(now);

            nodes.suspectNode(node);
            createSuspectTask(toSuspectNode, now);
        } finally {
            node.unlock();
        }
    }


    private void createSuspectTask(String nodeId, Date suspectTime) {
        SuspectTask task = new SuspectTask(nodeId, nodes, this, suspectTime);
        suspectTaskMap.put(nodeId, task);
        ScheduledFuture<?> future = executorService.schedule(task, suspectTimeout, TimeUnit.MILLISECONDS);
        task.setFuture(future);
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

            messageQueue.enqueue(message);
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

    @Override
    public void setMessageNotifier(GossipMessageNotifier messageNotifier) {
        this.messageNotifier = messageNotifier;
    }

    @Override
    public void handle(GossipRequest request) {
        List<GossipMessage> messages = request.getMessages();
        for (GossipMessage message : messages) {
            switch (message.getType()) {
                case GossipMessage.ALIVE:
                    aliveNode((AliveMessage) message, false);
                    break;
                case GossipMessage.SUSPECT:
                    suspectNode((SuspectMessage) message);
                    break;
                case GossipMessage.DEAD:
                    deadNode((DeadMessage) message);
                    break;
                case USER:
                    messageNotifier.notify(message);
                    break;
                default:
                    logger.error("unknown gossip message type:{}", message.getType());
                    throw new RuntimeException("unknown gossip message type:" + message.getType());
            }
        }
    }
}

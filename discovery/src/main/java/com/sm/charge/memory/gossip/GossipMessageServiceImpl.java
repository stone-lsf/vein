package com.sm.charge.memory.gossip;

import com.sm.charge.memory.Node;
import com.sm.charge.memory.NodeListener;
import com.sm.charge.memory.NodeStatus;
import com.sm.charge.memory.Nodes;
import com.sm.charge.memory.ServerContext;
import com.sm.charge.memory.gossip.messages.AliveMessage;
import com.sm.charge.memory.gossip.messages.DeadMessage;
import com.sm.charge.memory.gossip.messages.GossipMessage;
import com.sm.charge.memory.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.sm.charge.memory.NodeStatus.ALIVE;
import static com.sm.charge.memory.NodeStatus.DEAD;
import static com.sm.charge.memory.NodeStatus.SUSPECT;
import static com.sm.charge.memory.gossip.messages.GossipMessage.USER;


/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:23
 */
public class GossipMessageServiceImpl extends LoggerSupport implements GossipMessageService {
    private final CopyOnWriteArrayList<NodeListener> listeners = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, SuspectTask> suspectTaskMap = new ConcurrentHashMap<>();

    private final Nodes nodes;
    private final ServerContext serverContext;
    private final MessageQueue messageQueue;
    private final int suspectTimeout;

    private final ScheduledExecutorService executorService;

    private GossipMessageNotifier messageNotifier;

    public GossipMessageServiceImpl(Nodes nodes, ServerContext serverContext, MessageQueue messageQueue, int suspectTimeout) {
        this.nodes = nodes;
        this.serverContext = serverContext;
        this.messageQueue = messageQueue;
        this.suspectTimeout = suspectTimeout;
        this.executorService = serverContext.getExecutorService();
    }

    @Override
    public void aliveNode(AliveMessage message, boolean bootstrap) {
        String toAliveNode = message.getNodeId();
        logger.info("node:{} receive alive message to node:{} ,bootstrap:{}", nodes.getSelf(), toAliveNode, bootstrap);

        Node node = new Node(message, DEAD, new Date());
        Node existNode = nodes.addIfAbsent(node);
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

            long existIncarnation = existNode.getIncarnation();
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

            NodeStatus oldStatus = node.getStatus();
            if (oldStatus != ALIVE) {
                node.setIncarnation(aliveIncarnation);
                node.setStatus(ALIVE);
                node.setStatusChangeTime(new Date());
            }

            nodes.aliveNode(existNode);
            messageQueue.enqueue(message);

            for (NodeListener listener : listeners) {
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
        Node localNode = nodes.getLocalNode();

        long newIncarnation = incarnation + 1;
        localNode.setIncarnation(newIncarnation);

        AliveMessage message = new AliveMessage(localNode.getNodeId(), localNode.getAddress(), newIncarnation, localNode.getType());
        messageQueue.enqueue(message);
    }

    private Connection createConnection(Address address) {
        Connection connection = serverContext.getConnection(address);
        if (connection != null) {
            return connection;
        }
        return serverContext.createConnection(address);
    }

    @Override
    public void suspectNode(SuspectMessage message) {
        String toSuspectNode = message.getNodeId();
        logger.info("node:{} receive suspect message to node:{}", nodes.getSelf(), toSuspectNode);

        Node node = nodes.get(toSuspectNode);
        if (node == null) {
            logger.warn("receive suspect message of node:{},but it isn't in nodes", toSuspectNode);
            return;
        }

        node.lock();
        try {
            long suspectIncarnation = message.getIncarnation();
            long existIncarnation = node.getIncarnation();

            if (suspectIncarnation < existIncarnation) {
                logger.warn("receive suspect message of node:{}, suspect incarnation:{} less than current:{}", toSuspectNode, suspectIncarnation, existIncarnation);
                return;
            }

            NodeStatus oldStatus = node.getStatus();
            if (oldStatus != ALIVE) {
                logger.warn("receive suspect message of node:{}, current status is {}", toSuspectNode, oldStatus);
                return;
            }

            if (nodes.isLocalNode(toSuspectNode)) {
                logger.warn("refute a suspect message from {}", message.getFrom());
                refute(message.getIncarnation());
                return;
            }

            messageQueue.enqueue(message);

            node.setIncarnation(suspectIncarnation);
            node.setStatus(SUSPECT);
            Date now = new Date();
            node.setStatusChangeTime(now);

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
        logger.info("node:{} receive dead message to node:{}", nodes.getSelf(), toDeadNode);

        Node node = nodes.get(toDeadNode);
        if (node == null) {
            logger.warn("receive dead message of node:{},but it isn't in nodes", toDeadNode);
            return;
        }

        node.lock();
        try {
            long deadIncarnation = message.getIncarnation();
            long existIncarnation = node.getIncarnation();

            if (deadIncarnation < existIncarnation) {
                logger.warn("receive dead message of node:{}, dead incarnation:{} less than current:{}", toDeadNode, deadIncarnation, existIncarnation);
                return;
            }

            NodeStatus oldStatus = node.getStatus();
            if (oldStatus == DEAD) {
                logger.warn("receive dead message of node:{}, current status is dead", toDeadNode);
                return;
            }

            if (nodes.isLocalNode(toDeadNode)) {
                logger.warn("refute a dead message from {}", message.getFrom());
                refute(deadIncarnation);
                return;
            }

            logger.info("equeue dead message:{}", message);
            messageQueue.enqueue(message);
            node.setStatus(DEAD);
            node.setIncarnation(deadIncarnation);
            node.setStatusChangeTime(new Date());

            nodes.deadNode(node);
            for (NodeListener listener : listeners) {
                listener.onLeave(node);
            }
        } finally {
            node.unlock();
        }
    }

    @Override
    public void addListener(NodeListener listener) {
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
                    logger.error("unknown com.sm.charge.memory.gossip message type:{}", message.getType());
                    throw new RuntimeException("unknown com.sm.charge.memory.gossip message type:" + message.getType());
            }
        }
    }
}

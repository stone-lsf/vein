package com.sm.charge.memory;

import com.sm.charge.memory.gossip.GossipMessageService;
import com.sm.charge.memory.gossip.GossipMessageServiceImpl;
import com.sm.charge.memory.gossip.GossipTask;
import com.sm.charge.memory.gossip.MessageQueue;
import com.sm.charge.memory.gossip.messages.AliveMessage;
import com.sm.charge.memory.handler.GossipRequestHandler;
import com.sm.charge.memory.handler.PingMessageHandler;
import com.sm.charge.memory.handler.PushPullRequestHandler;
import com.sm.charge.memory.handler.RedirectPingHandler;
import com.sm.charge.memory.probe.ProbeServiceImpl;
import com.sm.charge.memory.probe.ProbeService;
import com.sm.charge.memory.probe.ProbeTask;
import com.sm.charge.memory.pushpull.PushPullService;
import com.sm.charge.memory.pushpull.PushPullServiceImpl;
import com.sm.charge.memory.pushpull.PushPullTask;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.utils.AddressUtil;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportFactory;
import com.sm.finance.charge.transport.api.TransportServer;
import com.sm.finance.charge.transport.api.exceptions.BindException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.sm.charge.memory.NodeStatus.ALIVE;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午9:00
 */
public class DiscoveryServiceImpl extends AbstractService implements DiscoveryService {

    private final Node localNode;
    private final ServerContext serverContext;
    private final DiscoveryConfig config;
    private final Nodes nodes;

    private final MessageQueue messageQueue;
    private volatile boolean joined = false;
    private GossipMessageService gossipMessageService;
    private PushPullService pushPullService;
    private ProbeService probeService;


    private volatile ScheduledFuture probeFuture;
    private volatile ScheduledFuture gossipFuture;
    private volatile ScheduledFuture pushPullFuture;

    public DiscoveryServiceImpl(DiscoveryConfig config) {
        this.config = config;
        Address address = AddressUtil.getLocalAddress(config.getBindPort());

        NodeType type = NodeType.valueOf(config.getNodeType());
        localNode = new Node(config.getNodeId(), address, type, 0, new Date(), ALIVE);

        Transport transport = TransportFactory.create(config.getTransportType());
        TransportClient client = transport.client();
        TransportServer server = transport.server();

        this.serverContext = new ServerContext(localNode.getNodeId(), client, server);

        nodes = new Nodes(config.getNodeId());
        messageQueue = new MessageQueue(config.getGossipQueueSize());
    }


    @Override
    public synchronized boolean join(String cluster) {
        if (joined) {
            return true;
        }
        logger.info("start join cluster:{}", cluster);

        String members = config.getMembers();
        List<Address> addresses = AddressUtil.parseList(members);
        int success = 0;
        for (Address address : addresses) {
            try {
                pushPullService.pushPull(address);
                success++;
            } catch (Exception e) {
                logger.error("push and pull message from node[{}] failed,cased by ", address, e);
            }
        }

        return success > 0;
    }

    @Override
    public Nodes getNodes() {
        return nodes;
    }

    @Override
    protected void doStart() throws Exception {
        int port = config.getBindPort();
        TransportServer transportServer;
        try {
            transportServer = serverContext.getServer();
            transportServer.listen(port, connection -> logger.info("receive connection[{}]", connection.getConnectionId()));
        } catch (BindException e) {
            logger.error("bind port failed", e);
            throw new RuntimeException("init node service fail", e);
        }

        gossipMessageService = new GossipMessageServiceImpl(nodes, serverContext, messageQueue, config.getSuspectTimeout());
        pushPullService = new PushPullServiceImpl(nodes, serverContext, gossipMessageService);
        probeService = new ProbeServiceImpl(nodes, config.getIndirectNodeNum());

        ConnectionManager manager = transportServer.getConnectionManager();
        manager.registerMessageHandler(new PushPullRequestHandler(pushPullService));
        manager.registerMessageHandler(new PingMessageHandler(probeService));
        manager.registerMessageHandler(new RedirectPingHandler(probeService));
        manager.registerMessageHandler(new GossipRequestHandler(gossipMessageService));

        AliveMessage message = new AliveMessage(localNode.getNodeId(), localNode.getAddress(), localNode.nextIncarnation(), localNode.getType());
        gossipMessageService.aliveNode(message, true);

        doSchedule();
    }

    private void doSchedule() {
        ScheduledExecutorService executorService = serverContext.getExecutorService();

        ProbeTask probeTask = new ProbeTask(nodes, probeService, config, gossipMessageService);
        int probeInterval = config.getProbeInterval();
        probeFuture = executorService.scheduleWithFixedDelay(probeTask, probeInterval, probeInterval, TimeUnit.MILLISECONDS);


        GossipTask gossipTask = new GossipTask(nodes, messageQueue, config);
        int gossipInterval = config.getGossipInterval();
        gossipFuture = executorService.scheduleWithFixedDelay(gossipTask, gossipInterval, gossipInterval, TimeUnit.MILLISECONDS);

        PushPullTask pushPullTask = new PushPullTask(nodes, pushPullService);
        int pushPullInterval = config.getPushPullInterval();
        pushPullFuture = executorService.scheduleWithFixedDelay(pushPullTask, pushPullInterval, pushPullInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doClose() throws Exception {

        cancelFutures();

        serverContext.getClient().close();
        serverContext.getServer().close();

        joined = false;
    }


    private void cancelFutures() {
        if (probeFuture != null) {
            probeFuture.cancel(false);
        }

        if (gossipFuture != null) {
            gossipFuture.cancel(false);
        }

        if (pushPullFuture != null) {
            pushPullFuture.cancel(false);
        }
    }
}

package com.sm.finance.charge.cluster.discovery;

import com.sm.finance.charge.cluster.discovery.gossip.GossipMessageController;
import com.sm.finance.charge.cluster.discovery.gossip.GossipMessageService;
import com.sm.finance.charge.cluster.discovery.gossip.GossipTask;
import com.sm.finance.charge.cluster.discovery.gossip.MessageQueue;
import com.sm.finance.charge.cluster.discovery.gossip.messages.AliveMessage;
import com.sm.finance.charge.cluster.discovery.handler.GossipMessagesHandler;
import com.sm.finance.charge.cluster.discovery.handler.PingMessageHandler;
import com.sm.finance.charge.cluster.discovery.handler.PushPullRequestHandler;
import com.sm.finance.charge.cluster.discovery.handler.RedirectPingHandler;
import com.sm.finance.charge.cluster.discovery.probe.ProbeController;
import com.sm.finance.charge.cluster.discovery.probe.ProbeService;
import com.sm.finance.charge.cluster.discovery.probe.ProbeTask;
import com.sm.finance.charge.cluster.discovery.pushpull.PushPullController;
import com.sm.finance.charge.cluster.discovery.pushpull.PushPullService;
import com.sm.finance.charge.cluster.discovery.pushpull.PushPullTask;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.AddressUtil;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportFactory;
import com.sm.finance.charge.transport.api.TransportServer;
import com.sm.finance.charge.transport.api.exceptions.BindException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午9:00
 */
public class DefaultDiscoveryService extends AbstractService implements DiscoveryService {
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final DiscoveryNode localNode;
    private final DiscoveryConfig config;
    private final DiscoveryNodes nodes;

    private final MessageQueue messageQueue;
    private volatile boolean joined = false;
    private GossipMessageService gossipMessageService;
    private PushPullService pushPullService;
    private ProbeService probeService;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(PROCESSORS + 1, new NamedThreadFactory("DiscoveryPool"));

    private volatile ScheduledFuture probeFuture;
    private volatile ScheduledFuture gossipFuture;
    private volatile ScheduledFuture pushPullFuture;

    public DefaultDiscoveryService(DiscoveryConfig config) {
        this.config = config;
        Address address = AddressUtil.getLocalAddress(config.getPort());

        DiscoveryNodeState nodeState = new DiscoveryNodeState(config.getNodeId(), 0, DiscoveryNode.Status.ALIVE, new Date());
        localNode = new DiscoveryNode(config.getNodeId(), address, nodeState, DiscoveryNode.Type.valueOf(config.getType()));

        Transport transport = TransportFactory.create(config.getTransportType());
        TransportClient transportClient = transport.client();
        TransportServer transportServer = transport.server();

        localNode.setTransportClient(transportClient);
        localNode.setTransportServer(transportServer);

        nodes = new DiscoveryNodes(config.getNodeId());
        messageQueue = new MessageQueue(config.getGossipQueueSize());
    }


    @Override
    public synchronized boolean join(String cluster) {
        if (joined) {
            return true;
        }

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
    public DiscoveryNodes getNodes() {
        return nodes;
    }

    @Override
    protected void doStart() throws Exception {
        int port = config.getPort();
        TransportServer transportServer;
        try {
            transportServer = localNode.getTransportServer();
            transportServer.listen(port, connection -> logger.info("receive connection[{}]", connection.getConnectionId()));
        } catch (BindException e) {
            logger.error("bind port failed", e);
            throw new RuntimeException("init node service fail", e);
        }

        TransportClient client = localNode.getTransportClient();
        gossipMessageService = new GossipMessageController(nodes, client, messageQueue, config.getSuspectTimeout(), executorService);
        pushPullService = new PushPullController(nodes, client, gossipMessageService);
        probeService = new ProbeController(nodes, config.getIndirectNodeNum());

        ConnectionManager manager = transportServer.getConnectionManager();
        manager.registerMessageHandler(new PushPullRequestHandler(pushPullService));
        manager.registerMessageHandler(new PingMessageHandler(probeService));
        manager.registerMessageHandler(new RedirectPingHandler(probeService));
        manager.registerMessageHandler(new GossipMessagesHandler(gossipMessageService));


        DiscoveryNodeState state = localNode.getState();
        AliveMessage message = new AliveMessage(localNode.getNodeId(), localNode.getAddress(), state.nextIncarnation(), localNode.getType());
        gossipMessageService.aliveNode(message, true);

        doSchedule();
    }

    private void doSchedule() {
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
    protected void doClose() {

        cancelFutures();

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

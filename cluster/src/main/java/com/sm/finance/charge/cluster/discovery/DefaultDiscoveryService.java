package com.sm.finance.charge.cluster.discovery;

import com.sm.finance.charge.cluster.discovery.gossip.GossipMessageService;
import com.sm.finance.charge.cluster.discovery.gossip.messages.AliveMessage;
import com.sm.finance.charge.cluster.discovery.handler.PushPullRequestHandler;
import com.sm.finance.charge.cluster.discovery.pushpull.PushPullService;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.AddressUtil;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportFactory;
import com.sm.finance.charge.transport.api.TransportServer;
import com.sm.finance.charge.transport.api.exceptions.BindException;

import java.util.Date;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午9:00
 */
public class DefaultDiscoveryService extends AbstractService implements DiscoveryService {

    private final DiscoveryNode localNode;
    private final DiscoveryConfig config;
    private final DiscoveryNodes nodes;
    private volatile boolean joined = false;
    private GossipMessageService nodeStatusService;
    private PushPullService pushPullService;

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

        ConnectionManager manager = transportServer.getConnectionManager();
        manager.registerMessageHandler(new PushPullRequestHandler(pushPullService));
//        manager.registerMessageHandler(new PingMessageHandler(membershipController));
//        manager.registerMessageHandler(new GossipMessagesHandler(membershipController));
//        manager.registerMessageHandler(new RedirectPingHandler(membershipController));

        DiscoveryNodeState state = localNode.getState();
        AliveMessage message = new AliveMessage(localNode.getNodeId(), localNode.getAddress(), state.nextIncarnation(), localNode.getType());
        nodeStatusService.aliveNode(message, true);
    }

    @Override
    protected void doClose() {

    }
}

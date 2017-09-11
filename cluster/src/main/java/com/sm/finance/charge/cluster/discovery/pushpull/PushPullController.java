package com.sm.finance.charge.cluster.discovery.pushpull;

import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;
import com.sm.finance.charge.cluster.discovery.gossip.NodeStatusService;
import com.sm.finance.charge.cluster.discovery.gossip.messages.AliveMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:27
 */
public class PushPullController extends LogSupport implements PushPullService {

    private DiscoveryNodes nodes;
    private TransportClient transportClient;
    private NodeStatusService nodeStatusService;
    private ConcurrentMap<Address, Connection> connectionMap = new ConcurrentHashMap<>();

    public PushPullController(DiscoveryNodes nodes, TransportClient transportClient) {
        this.nodes = nodes;
        this.transportClient = transportClient;
    }

    @Override
    public void pushPull(Address address) throws Exception {
        List<PushNodeState> states = sendLocalState(address);
        mergeRemoteState(states);
    }

    private List<PushNodeState> sendLocalState(Address nodeAddress) throws Exception {
        List<PushNodeState> states = nodes.buildPushNodeStates();

        PushPullRequest request = new PushPullRequest(nodes.getLocalNodeId(), states);
        Connection connection = connectionMap.get(nodeAddress);
        if (connection == null) {
            connection = transportClient.connect(nodeAddress, 3);
            if (connection == null) {
                return Collections.emptyList();
            }

            Connection existConnection = connectionMap.putIfAbsent(nodeAddress, connection);
            if (existConnection != null) {
                try {
                    existConnection.close();
                } catch (Exception e) {
                    logger.warn("close connection:{} caught exception:{}", existConnection, e);
                }
                connection = existConnection;
            }
        }

        return connection.syncRequest(request);
    }

    private void mergeRemoteState(List<PushNodeState> states) {
        for (PushNodeState state : states) {
            switch (state.getNodeStatus()) {
                case ALIVE:
                    AliveMessage aliveMessage = new AliveMessage(state);
                    nodeStatusService.aliveNode(aliveMessage, false);
                    break;
                case SUSPECT:
                case DEAD:
                    SuspectMessage suspectMessage = new SuspectMessage(state, nodes.getLocalNodeId());
                    nodeStatusService.suspectNode(suspectMessage);
                    break;
            }
        }
    }

    @Override
    public PushPullResponse handle(PushPullRequest request) {
        return null;
    }
}

package com.sm.finance.charge.cluster.discovery.pushpull;

import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;
import com.sm.finance.charge.cluster.discovery.gossip.GossipMessageService;
import com.sm.finance.charge.cluster.discovery.gossip.messages.AliveMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:27
 */
public class PushPullController extends LoggerSupport implements PushPullService {

    private final DiscoveryNodes nodes;
    private final TransportClient transportClient;
    private final GossipMessageService gossipMessageService;
    private ConcurrentMap<Address, Connection> connectionMap = new ConcurrentHashMap<>();

    public PushPullController(DiscoveryNodes nodes, TransportClient transportClient, GossipMessageService gossipMessageService) {
        this.nodes = nodes;
        this.transportClient = transportClient;
        this.gossipMessageService = gossipMessageService;
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
            connection = transportClient.connect(nodeAddress, 3).handle((conn, error) -> {
                if (error != null) {
                    return null;
                }
                return conn;
            }).join();

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
                    gossipMessageService.aliveNode(aliveMessage, false);
                    break;
                case SUSPECT:
                case DEAD:
                    SuspectMessage suspectMessage = new SuspectMessage(state, nodes.getLocalNodeId());
                    gossipMessageService.suspectNode(suspectMessage);
                    break;
            }
        }
    }

    @Override
    public CompletableFuture<PushPullResponse> handle(PushPullRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            List<PushNodeState> states = nodes.buildPushNodeStates();
            mergeRemoteState(request.getStates());
            return new PushPullResponse(states);
        });
    }

    @Override
    public void handle(PushPullResponse response) {
        mergeRemoteState(response.getStates());
    }
}

package com.sm.charge.memory.pushpull;

import com.sm.charge.memory.DiscoveryNodes;
import com.sm.charge.memory.DiscoveryServerContext;
import com.sm.charge.memory.gossip.GossipMessageService;
import com.sm.charge.memory.gossip.messages.AliveMessage;
import com.sm.charge.memory.gossip.messages.SuspectMessage;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:27
 */
public class PushPullServiceImpl extends LoggerSupport implements PushPullService {

    private final DiscoveryNodes nodes;
    private final DiscoveryServerContext serverContext;
    private final GossipMessageService gossipMessageService;

    public PushPullServiceImpl(DiscoveryNodes nodes, DiscoveryServerContext serverContext, GossipMessageService gossipMessageService) {
        this.nodes = nodes;
        this.serverContext = serverContext;
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
        Connection connection = serverContext.getConnection(nodeAddress);
        if (connection == null) {
            connection = serverContext.createConnection(nodeAddress, 3);
            if (connection == null) {
                logger.error("create connection to {} failed", nodeAddress);
                throw new RuntimeException("can't connect to:" + nodeAddress);
            }
        }

        PushPullResponse response = connection.syncRequest(request);
        return response.getStates();
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

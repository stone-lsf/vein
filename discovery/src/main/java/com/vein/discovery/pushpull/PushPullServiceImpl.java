package com.vein.discovery.pushpull;

import com.vein.discovery.Nodes;
import com.vein.discovery.ServerContext;
import com.vein.discovery.gossip.GossipMessageService;
import com.vein.discovery.gossip.messages.AliveMessage;
import com.vein.discovery.gossip.messages.SuspectMessage;
import com.vein.common.Address;
import com.vein.common.base.LoggerSupport;
import com.vein.transport.api.Connection;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:27
 */
public class PushPullServiceImpl extends LoggerSupport implements PushPullService {

    private final Nodes nodes;
    private final ServerContext serverContext;
    private final GossipMessageService gossipMessageService;

    public PushPullServiceImpl(Nodes nodes, ServerContext serverContext, GossipMessageService gossipMessageService) {
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

        PushPullRequest request = new PushPullRequest(nodes.getSelf(), states);
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
            switch (state.getStatus()) {
                case ALIVE:
                    AliveMessage aliveMessage = new AliveMessage(state);
                    gossipMessageService.aliveNode(aliveMessage, false);
                    break;
                case SUSPECT:
                case DEAD:
                    SuspectMessage suspectMessage = new SuspectMessage(state, nodes.getSelf());
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

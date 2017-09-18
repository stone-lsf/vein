package com.sm.finance.charge.cluster.discovery.probe;

import com.sm.finance.charge.cluster.discovery.DiscoveryNode;
import com.sm.finance.charge.cluster.discovery.DiscoveryNodes;
import com.sm.finance.charge.cluster.discovery.NodeFilter;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.Merger;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.exceptions.RemoteException;
import com.sm.finance.charge.transport.api.exceptions.TimeoutException;
import com.sm.finance.charge.transport.api.handler.AbstractExceptionResponseHandler;
import com.sm.finance.charge.transport.api.support.ResponseContext;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午2:50
 */
public class ProbeController extends LogSupport implements ProbeService {
    private DiscoveryNodes nodes;
    private NodeFilter nodeFilter;
    private int indirectNodeNum;

    @Override
    public Ack ping(DiscoveryNode node, int timeout) {
        Connection connection = node.getConnection();
        String target = node.getNodeId();
        if (connection == null) {
            logger.error("node:{} hasn't connection", target);
            throw new IllegalStateException("node:" + target + " hasn't connection");
        }

        Ping ping = new Ping(nodes.getLocalNodeId());
        try {
            return connection.syncRequest(ping, timeout);
        } catch (Exception e) {
            logger.error("send ping to node:{} caught exception:{}", target, e);
            return null;
        }
    }

    @Override
    public boolean redirectPing(DiscoveryNode node, int timeout) {
        List<DiscoveryNode> randomNodes = nodes.randomNodes(indirectNodeNum, nodeFilter);
        String target = node.getNodeId();
        RedirectPing ping = new RedirectPing(nodes.getLocalNodeId(), target);

        Merger merge = new Merger(indirectNodeNum);
        for (DiscoveryNode randomNode : randomNodes) {
            Connection connection = randomNode.getConnection();
            String nodeId = randomNode.getNodeId();
            if (connection == null) {
                logger.error("node:{} hasn't connection", nodeId);
                throw new IllegalStateException("node:" + nodeId + " hasn't connection");
            }

            connection.send(ping, timeout, new AbstractExceptionResponseHandler<Ack>() {
                @Override
                protected void onRemoteException(RemoteException e, ResponseContext context) {
                    logger.error("redirect ping to target:{} by node:{} caught exception:{}", target, node, e);
                    merge.mergeFailure();
                }

                @Override
                protected void onTimeoutException(TimeoutException e, ResponseContext context) {
                    logger.error("redirect ping to target:{} by node:{} timeout:{}", target, node, e.getTimeout());
                    merge.mergeFailure();
                }

                @Override
                public void handle(Ack ack, Connection connection) {
                    merge.mergeSuccess();
                }
            });
        }

        try {
            return merge.anyOf();
        } catch (Exception e) {
            logger.error("redirect ping to node:{} caught exception:{}", target, e);
            return false;
        }
    }
}

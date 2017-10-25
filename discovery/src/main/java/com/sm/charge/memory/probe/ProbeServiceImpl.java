package com.sm.charge.memory.probe;

import com.sm.charge.memory.Node;
import com.sm.charge.memory.NodeFilter;
import com.sm.charge.memory.Nodes;
import com.sm.finance.charge.common.Merger;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.exceptions.RemoteException;
import com.sm.finance.charge.transport.api.exceptions.TimeoutException;
import com.sm.finance.charge.transport.api.handler.AbstractExceptionsHandler;
import com.sm.finance.charge.transport.api.support.ResponseContext;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.sm.charge.memory.NodeStatus.DEAD;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午2:50
 */
public class ProbeServiceImpl extends LoggerSupport implements ProbeService {
    private final Nodes nodes;
    private final int indirectNodeNum;

    public ProbeServiceImpl(Nodes nodes, int indirectNodeNum) {
        this.nodes = nodes;
        this.indirectNodeNum = indirectNodeNum;
    }

    @Override
    public Ack ping(Node node, int timeout) {
        Connection connection = node.getConnection();
        String target = node.getNodeId();
        if (connection == null) {
            logger.error("node:{} hasn't connection", target);
            throw new IllegalStateException("node:" + target + " hasn't connection");
        }

        Ping ping = new Ping(nodes.getSelf(), target);
        try {
            return connection.syncRequest(ping, timeout);
        } catch (Exception e) {
            logger.error("send ping to node:{} caught exception:{}", target, e);
            return null;
        }
    }

    @Override
    public boolean redirectPing(Node node, int timeout) {
        String target = node.getNodeId();
        List<Node> randomNodes = nodes.randomNodes(indirectNodeNum, new Filter(node));
        if (CollectionUtils.isEmpty(randomNodes)) {
            logger.info("can't find node to redirect node:{}", target);
            return false;
        }

        RedirectPing ping = new RedirectPing(nodes.getSelf(), target);
        Merger merge = new Merger(randomNodes.size());

        for (Node randomNode : randomNodes) {
            Connection connection = randomNode.getConnection();
            String nodeId = randomNode.getNodeId();
            if (connection == null) {
                logger.error("node:{} hasn't connection", nodeId);
                merge.mergeFailure();
                throw new IllegalStateException("node:" + nodeId + " hasn't connection");
            }

            connection.send(ping, timeout, new AbstractExceptionsHandler<Ack>() {
                @Override
                protected void onRemoteException(RemoteException e, ResponseContext context) {
                    logger.error("redirect ping to target:{} by node:{} caught exception:{}", target, randomNode.getNodeId(), e);
                    merge.mergeFailure();
                }

                @Override
                protected void onTimeoutException(TimeoutException e, ResponseContext context) {
                    logger.error("redirect ping to target:{} by node:{} timeout:{}", target, randomNode.getNodeId(), e.getTimeout());
                    merge.mergeFailure();
                }

                @Override
                public void handle(Ack ack, Connection connection) {
                    merge.mergeSuccess();
                }
            });
        }

        try {
            return merge.anyOf().get();
        } catch (Exception e) {
            logger.error("redirect ping to node:{} caught exception:{}", target, e);
            return false;
        }
    }

    @Override
    public CompletableFuture<Ack> handle(Ping ping) {
        String nodeId = ping.getTo();
        if (!nodeId.equals(nodes.getSelf())) {
            logger.error("receive ping message from:{},but target is node:{}", ping.getFrom(), nodeId);
            //FIXME 此时应该返回错误信息

            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(new Ack(nodes.getSelf()));
    }

    @Override
    public CompletableFuture<Ack> handle(RedirectPing redirectPing) {
        return CompletableFuture.supplyAsync(() -> {
            String target = redirectPing.getTarget();
            Node node = nodes.get(target);
            if (node == null) {
                logger.warn("receive redirect ping to node:{}, but current cluster state don't contain node", target);
                //FIXME 此时应该返回错误信息
                return null;
            }

            Connection connection = node.getConnection();
            if (connection == null) {
                logger.error("node:{} don't have connection", target);
                throw new IllegalStateException("node:" + target + " don't have connection");
            }

            Ping ping = new Ping(nodes.getSelf(), target);
            try {
                return connection.syncRequest(ping);
            } catch (Exception e) {
                logger.error("send ping to node[{}] for redirect ping caught exception:{}", target, e);
                //FIXME 此时应该返回错误信息
                return null;
            }
        });
    }


    private class Filter implements NodeFilter {

        private final Node target;

        private Filter(Node target) {
            this.target = target;
        }

        @Override
        public boolean apply(Node node) {
            String nodeId = node.getNodeId();
            return nodes.isLocalNode(nodeId) || target.getNodeId().equals(nodeId) || node.getStatus() == DEAD;

        }
    }
}

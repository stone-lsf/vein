package com.sm.charge.memory;

import com.sm.finance.charge.common.base.Configure;

import java.util.UUID;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午9:03
 */
public class DiscoveryConfig {

    /**
     * 节点名称
     */
    private static final String NODE_ID = "discovery.node.id";
    private static final String DEFAULT_NODE_ID = UUID.randomUUID().toString();

    /**
     * 绑定端口
     */
    private static final String BIND_PORT = "discovery.bind.port";
    private static final int DEFAULT_BIND_PORT = 56465;

    /**
     * push/pull间隔(ms)
     */
    private static final String PUSH_PULL_INTERVAL = "discovery.pushpull.interval";
    private static final int DEFAULT_PUSH_PULL_INTERVAL = 20000;

    /**
     * gossip间隔(ms)
     */
    private static final String GOSSIP_INTERVAL = "discovery.gossip.interval";
    private static final int DEFAULT_GOSSIP_INTERVAL = 600;

    /**
     * probe间隔(ms)
     */
    private static final String PROBE_INTERVAL = "discovery.probe.interval";
    private static final int DEFAULT_PROBE_INTERVAL = 600;

    /**
     * 成员列表，多个成员之间用逗号分隔
     */
    private static final String MEMBERS = "discovery.members";

    /**
     * 加入集群超时时间
     */
    private static final String JOIN_TIMEOUT = "discovery.join.timeout";
    private static final int DEFAULT_JOIN_TIMEOUT = 20000;

    /**
     * 传输方式
     */
    private static final String TRANSPORT_TYPE = "discovery.transport.type";
    private static final String DEFAULT_TRANSPORT_TYPE = "netty";

    /**
     * 节点类型
     */
    private static final String NODE_TYPE = "discovery.node.type";
    private static final byte DEFAULT_NODE_TYPE = 1;

    /**
     * ping超时时间(ms)
     */
    private static final String PING_TIMEOUT = "discovery.ping.timeout";
    private static final int DEFAULT_PING_TIMEOUT = 2000;

    /**
     * 间接ping超时时间(ms)
     */
    private static final String REDIRECT_PING_TIMEOUT = "discovery.ping.redirect.timeout";
    private static final int DEFAULT_REDIRECT_PING_TIMEOUT = 6000;

    /**
     * 猜疑超时时间
     */
    private static final String SUSPECT_TIMEOUT = "discovery.suspect.timeout";
    private static final int DEFAULT_SUSPECT_TIMEOUT = 6000;

    /**
     * gossip消息队列大小
     */
    private static final String GOSSIP_QUEUE_SIZE = "discovery.gossip.queue.size.max";
    private static final int DEFAULT_GOSSIP_QUEUE_SIZE = 6000;

    /**
     * 每次gossip的节点数
     */
    private static final String NODES_PER_GOSSIP = "discovery.gossip.nodes";
    private static final int DEFAULT_NODES_PER_GOSSIP = 1;

    /**
     * 每次gossip的最大消息数
     */
    private static final String MAX_GOSSIP_MESSAGE_COUNT = "discovery.gossip.message.count.max";
    private static final int DEFAULT_MAX_GOSSIP_MESSAGE_COUNT = 200;

    /**
     * 间接探测节点数
     */
    private static final String INDIRECT_NODE_NUM = "discovery.ping.redirect.node.count";
    private static final int DEFAULT_INDIRECT_NODE_NUM = 1;

    private final Configure configure;

    public DiscoveryConfig(Configure configure) {
        this.configure = configure;
    }

    public String getNodeId() {
        return configure.getString(NODE_ID, DEFAULT_NODE_ID);
    }


    public int getBindPort() {
        return configure.getInt(BIND_PORT, DEFAULT_BIND_PORT);
    }


    public int getPushPullInterval() {
        return configure.getInt(PUSH_PULL_INTERVAL, DEFAULT_PUSH_PULL_INTERVAL);
    }


    public int getGossipInterval() {
        return configure.getInt(GOSSIP_INTERVAL, DEFAULT_GOSSIP_INTERVAL);
    }


    public int getProbeInterval() {
        return configure.getInt(PROBE_INTERVAL, DEFAULT_PROBE_INTERVAL);
    }


    public String getMembers() {
        return configure.getString(MEMBERS);
    }


    public int getJoinTimeout() {
        return configure.getInt(JOIN_TIMEOUT, DEFAULT_JOIN_TIMEOUT);
    }


    public String getTransportType() {
        return configure.getString(TRANSPORT_TYPE, DEFAULT_TRANSPORT_TYPE);
    }


    public byte getNodeType() {
        return configure.getByte(NODE_TYPE, DEFAULT_NODE_TYPE);
    }


    public int getPingTimeout() {
        return configure.getInt(PING_TIMEOUT, DEFAULT_PING_TIMEOUT);
    }


    public int getRedirectPingTimeout() {
        return configure.getInt(REDIRECT_PING_TIMEOUT, DEFAULT_REDIRECT_PING_TIMEOUT);
    }


    public int getGossipQueueSize() {
        return configure.getInt(GOSSIP_QUEUE_SIZE, DEFAULT_GOSSIP_QUEUE_SIZE);
    }


    public int getNodesPerGossip() {
        return configure.getInt(NODES_PER_GOSSIP, DEFAULT_NODES_PER_GOSSIP);
    }


    public int getMaxGossipMessageCount() {
        return configure.getInt(MAX_GOSSIP_MESSAGE_COUNT, DEFAULT_MAX_GOSSIP_MESSAGE_COUNT);
    }


    public int getSuspectTimeout() {
        return configure.getInt(SUSPECT_TIMEOUT, DEFAULT_SUSPECT_TIMEOUT);
    }


    public int getIndirectNodeNum() {
        return configure.getInt(INDIRECT_NODE_NUM, DEFAULT_INDIRECT_NODE_NUM);
    }

}

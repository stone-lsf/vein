package com.sm.charge.memory;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午9:03
 */
public class DiscoveryConfig {

    /**
     * 节点名称
     */
    private String nodeId;

    /**
     * 绑定端口
     */
    private int port;

    /**
     * push/pull间隔(ms)
     */
    private int pushPullInterval;
    /**
     * gossip间隔(ms)
     */
    private int gossipInterval;

    /**
     * probe间隔(ms)
     */
    private int probeInterval;

    /**
     * 成员列表，多个成员之间用逗号分隔
     */
    private String members;

    /**
     * 加入集群超时时间
     */
    private int joinTimeout;

    /**
     * 传输方式
     */
    private String transportType;

    /**
     * 节点类型
     */
    private byte type;

    /**
     * ping超时时间(ms)
     */
    private int pingTimeout;

    /**
     * 间接ping超时时间(ms)
     */
    private int redirectPingTimeout;

    /**
     * 猜疑超时时间
     */
    private int suspectTimeout;

    /**
     * gossip消息队列大小
     */
    private int gossipQueueSize;

    /**
     * 每次gossip的节点数
     */
    private int nodesPerGossip;

    /**
     * 每次gossip的最大消息数
     */
    private int maxGossipMessageCount;

    /**
     * 间接探测节点数
     */
    private int indirectNodeNum;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPushPullInterval() {
        return pushPullInterval;
    }

    public void setPushPullInterval(int pushPullInterval) {
        this.pushPullInterval = pushPullInterval;
    }

    public int getGossipInterval() {
        return gossipInterval;
    }

    public void setGossipInterval(int gossipInterval) {
        this.gossipInterval = gossipInterval;
    }

    public int getProbeInterval() {
        return probeInterval;
    }

    public void setProbeInterval(int probeInterval) {
        this.probeInterval = probeInterval;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public int getJoinTimeout() {
        return joinTimeout;
    }

    public void setJoinTimeout(int joinTimeout) {
        this.joinTimeout = joinTimeout;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    public int getRedirectPingTimeout() {
        return redirectPingTimeout;
    }

    public void setRedirectPingTimeout(int redirectPingTimeout) {
        this.redirectPingTimeout = redirectPingTimeout;
    }

    public int getGossipQueueSize() {
        return gossipQueueSize;
    }

    public void setGossipQueueSize(int gossipQueueSize) {
        this.gossipQueueSize = gossipQueueSize;
    }

    public int getNodesPerGossip() {
        return nodesPerGossip;
    }

    public void setNodesPerGossip(int nodesPerGossip) {
        this.nodesPerGossip = nodesPerGossip;
    }

    public int getMaxGossipMessageCount() {
        return maxGossipMessageCount;
    }

    public void setMaxGossipMessageCount(int maxGossipMessageCount) {
        this.maxGossipMessageCount = maxGossipMessageCount;
    }

    public int getSuspectTimeout() {
        return suspectTimeout;
    }

    public void setSuspectTimeout(int suspectTimeout) {
        this.suspectTimeout = suspectTimeout;
    }

    public int getIndirectNodeNum() {
        return indirectNodeNum;
    }

    public void setIndirectNodeNum(int indirectNodeNum) {
        this.indirectNodeNum = indirectNodeNum;
    }
}

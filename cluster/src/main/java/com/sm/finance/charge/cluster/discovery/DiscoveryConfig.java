package com.sm.finance.charge.cluster.discovery;

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
}

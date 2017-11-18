package com.sm.charge.discovery.gossip.messages;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:27
 */
public class DeadMessage implements GossipContent {
    /**
     * 节点名称
     */
    private String nodeId;
    /**
     * 节点的incarnation
     */
    private long incarnation;

    /**
     * 消息来源
     */
    private String from;

    public DeadMessage() {
    }

    public DeadMessage(String nodeId, long incarnation, String from) {
        this.nodeId = nodeId;
        this.incarnation = incarnation;
        this.from = from;
    }


    @Override
    public short getType() {
        return DEAD;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public long getIncarnation() {
        return incarnation;
    }

    @Override
    public void setIncarnation(long incarnation) {
        this.incarnation = incarnation;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "DeadMessage{" +
            "nodeId='" + nodeId + '\'' +
            ", incarnation=" + incarnation +
            ", from='" + from + '\'' +
            '}';
    }
}

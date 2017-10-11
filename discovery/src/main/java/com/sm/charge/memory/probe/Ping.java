package com.sm.charge.memory.probe;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:08
 */
public class Ping {

    /**
     * ping消息来源节点
     */
    private String from;

    private String nodeId;

    public Ping() {
    }

    public Ping(String from, String nodeId) {
        this.from = from;
        this.nodeId = nodeId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "Ping{" +
            "from='" + from + '\'' +
            ", nodeId='" + nodeId + '\'' +
            '}';
    }
}

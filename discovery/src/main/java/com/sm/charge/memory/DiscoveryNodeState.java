package com.sm.charge.memory;

import java.util.Date;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:44
 */
public class DiscoveryNodeState {

    /**
     * 节点唯一标识符
     */
    private final String nodeId;

    /**
     * 自增的版本号
     */
    private volatile long incarnation;

    /**
     * 节点状态
     */
    private volatile DiscoveryNode.Status status;

    /**
     * 状态发生变化的时间
     */
    private volatile Date statusChangeTime;

    public DiscoveryNodeState(String nodeId, DiscoveryNode.Status status, Date statusChangeTime) {
        this.nodeId = nodeId;
        this.status = status;
        this.statusChangeTime = statusChangeTime;
    }

    public DiscoveryNodeState(String nodeId, long incarnation, DiscoveryNode.Status status, Date statusChangeTime) {
        this.nodeId = nodeId;
        this.incarnation = incarnation;
        this.status = status;
        this.statusChangeTime = statusChangeTime;
    }

    public String getNodeId() {
        return nodeId;
    }

    public long getIncarnation() {
        return incarnation;
    }

    public synchronized long nextIncarnation() {
        incarnation++;
        return incarnation;
    }

    public void setIncarnation(long incarnation) {
        this.incarnation = incarnation;
    }

    public DiscoveryNode.Status getStatus() {
        return status;
    }

    public void setStatus(DiscoveryNode.Status status) {
        this.status = status;
    }

    public Date getStatusChangeTime() {
        return statusChangeTime;
    }

    public void setStatusChangeTime(Date statusChangeTime) {
        this.statusChangeTime = statusChangeTime;
    }
}

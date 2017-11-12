package com.sm.charge.discovery.pushpull;

import com.sm.charge.discovery.NodeStatus;
import com.sm.charge.discovery.NodeType;
import com.sm.finance.charge.common.Address;


/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:26
 */
public class PushNodeState {
    private String nodeId;
    private Address address;
    private long incarnation;
    private NodeStatus status;
    private NodeType type;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public long getIncarnation() {
        return incarnation;
    }

    public void setIncarnation(long incarnation) {
        this.incarnation = incarnation;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "PushNodeState{" +
            "nodeId='" + nodeId + '\'' +
            ", address=" + address +
            ", incarnation=" + incarnation +
            ", status=" + status +
            ", type=" + type +
            '}';
    }
}

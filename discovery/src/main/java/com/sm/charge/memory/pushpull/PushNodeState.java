package com.sm.charge.memory.pushpull;

import com.sm.charge.memory.DiscoveryNode;
import com.sm.finance.charge.common.Address;


/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:26
 */
public class PushNodeState {
    private String nodeId;
    private Address address;
    private long incarnation;
    private DiscoveryNode.Status nodeStatus;
    private DiscoveryNode.Type nodeType;

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

    public DiscoveryNode.Status getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(DiscoveryNode.Status nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public DiscoveryNode.Type getNodeType() {
        return nodeType;
    }

    public void setNodeType(DiscoveryNode.Type nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public String toString() {
        return "PushNodeState{" +
            "nodeId='" + nodeId + '\'' +
            ", address=" + address +
            ", incarnation=" + incarnation +
            ", nodeStatus=" + nodeStatus +
            ", nodeType=" + nodeType +
            '}';
    }
}

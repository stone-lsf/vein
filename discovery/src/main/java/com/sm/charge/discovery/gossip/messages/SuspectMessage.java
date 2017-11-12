package com.sm.charge.discovery.gossip.messages;

import com.sm.charge.discovery.pushpull.PushNodeState;
import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:28
 */
public class SuspectMessage implements GossipContent {

    private String nodeId;
    private Address address;

    /**
     * 版本信息
     */
    private long incarnation;

    /**
     * suspect 发起的节点
     */
    private String from;

    public SuspectMessage() {
    }

    public SuspectMessage(String nodeId, Address address, long incarnation, String from) {
        this.nodeId = nodeId;
        this.address = address;
        this.incarnation = incarnation;
        this.from = from;
    }

    public SuspectMessage(PushNodeState state, String from) {
        this.nodeId = state.getNodeId();
        this.address = state.getAddress();
        this.incarnation = state.getIncarnation();
        this.from = from;
    }


    @Override
    public short getType() {
        return SUSPECT;
    }

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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "SuspectMessage{" +
            "nodeId='" + nodeId + '\'' +
            ", address=" + address +
            ", incarnation=" + incarnation +
            ", from='" + from + '\'' +
            '}';
    }
}

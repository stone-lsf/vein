package com.sm.charge.raft.server.events;

import com.sm.charge.raft.server.RaftMember;
import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/11/12 下午4:44
 */
public class MemberInfo {

    private String nodeId;

    private Address address;

    public MemberInfo() {
    }

    public MemberInfo(RaftMember member) {
        this.nodeId = member.getNodeId();
        this.address = member.getAddress();
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
}

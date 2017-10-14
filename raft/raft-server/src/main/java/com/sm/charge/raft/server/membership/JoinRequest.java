package com.sm.charge.raft.server.membership;

import com.sm.charge.raft.server.RaftMember;
import com.sm.finance.charge.common.Address;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:48
 */
public class JoinRequest {

    private long id;

    private Address address;

    private List<RaftMember> members;

    public JoinRequest() {
    }

    public JoinRequest(long id, Address address, List<RaftMember> members) {
        this.id = id;
        this.address = address;
        this.members = members;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<RaftMember> getMembers() {
        return members;
    }

    public void setMembers(List<RaftMember> members) {
        this.members = members;
    }
}

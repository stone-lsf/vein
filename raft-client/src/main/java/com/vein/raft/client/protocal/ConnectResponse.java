package com.vein.raft.client.protocal;

import com.vein.common.Address;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/11/18 上午11:57
 */
public class ConnectResponse {

    private Address leader;

    private List<Address> members;

    public Address getLeader() {
        return leader;
    }

    public void setLeader(Address leader) {
        this.leader = leader;
    }

    public List<Address> getMembers() {
        return members;
    }

    public void setMembers(List<Address> members) {
        this.members = members;
    }
}

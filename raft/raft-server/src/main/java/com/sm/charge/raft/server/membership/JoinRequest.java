package com.sm.charge.raft.server.membership;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:48
 */
public class JoinRequest {

    private String memberId;

    private Address address;

    public JoinRequest() {
    }

    public JoinRequest(String memberId, Address address) {
        this.memberId = memberId;
        this.address = address;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

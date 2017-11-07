package com.sm.charge.raft.client;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/10/18 上午12:54
 */
public class Configure implements Command {
    public static final byte JOIN = 1;
    public static final byte LEAVE = -1;

    private byte action;

    private String memberId;

    private Address address;

    public Configure() {
    }

    public Configure(byte action, String memberId, Address address) {
        this.action = action;
        this.memberId = memberId;
        this.address = address;
    }

    public byte getAction() {
        return action;
    }

    public void setAction(byte action) {
        this.action = action;
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

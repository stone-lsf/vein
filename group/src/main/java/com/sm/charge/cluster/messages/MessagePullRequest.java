package com.sm.charge.cluster.messages;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午10:33
 */
public class MessagePullRequest {
    private Address address;

    private long nextIndex;

    public MessagePullRequest() {
    }

    public MessagePullRequest(Address address, long nextIndex) {
        this.address = address;
        this.nextIndex = nextIndex;
    }

    public long getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(long nextIndex) {
        this.nextIndex = nextIndex;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

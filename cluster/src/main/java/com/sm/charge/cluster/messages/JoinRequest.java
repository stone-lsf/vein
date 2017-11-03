package com.sm.charge.cluster.messages;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午8:16
 */
public class JoinRequest {

    private Address address;
    private long version;
    private long lastIndex;

    public JoinRequest() {
    }

    public JoinRequest(Address address, long version, long lastIndex) {
        this.address = address;
        this.version = version;
        this.lastIndex = lastIndex;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(long lastIndex) {
        this.lastIndex = lastIndex;
    }
}

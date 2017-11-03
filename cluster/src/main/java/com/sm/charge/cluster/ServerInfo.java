package com.sm.charge.cluster;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/11/3 下午4:55
 */
public class ServerInfo {

    private String serverId;

    private Address address;

    public ServerInfo() {
    }

    public ServerInfo(String serverId, Address address) {
        this.serverId = serverId;
        this.address = address;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

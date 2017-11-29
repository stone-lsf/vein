package com.vein.cluster;

import com.vein.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/11/3 下午4:55
 */
public class ServerInfo {

    private String serverId;

    private Address address;

    private ServerType type;

    public ServerInfo() {
    }

    public ServerInfo(String serverId, Address address, ServerType type) {
        this.serverId = serverId;
        this.address = address;
        this.type = type;
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

    public ServerType getType() {
        return type;
    }

    public void setType(ServerType type) {
        this.type = type;
    }
}

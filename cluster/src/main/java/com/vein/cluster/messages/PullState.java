package com.vein.cluster.messages;

import com.vein.common.Address;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午4:00
 */
public class PullState {
    private static final AtomicLong idGenerator = new AtomicLong();

    /**
     * 自增id
     */
    private long id;
    private String serverId;
    private Address address;
    private long version;
    private long lastIndex;
    private boolean leader;

    public PullState(Address address, String serverId, long lastIndex, long version) {
        this.address = address;
        this.serverId = serverId;
        this.lastIndex = lastIndex;
        this.version = version;
        this.id = idGenerator.incrementAndGet();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }
}

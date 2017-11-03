package com.sm.charge.cluster;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.BaseNode;
import com.sm.finance.charge.transport.api.Connection;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午10:28
 */
public class Server extends BaseNode<String> {

    private volatile long version;

    private volatile boolean leader;

    /**
     * 水位
     */
    private volatile long watermark;

    /**
     * 已经拉取的index
     */
    private volatile long pullIndex;

    private volatile Connection connection;

    public Server(String serverId, Address address) {
        super(serverId, address);
    }

    public String getServerId() {
        return getNodeId();
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }


    public Connection getConnection() {
        return connection;
    }

    public long getWatermark() {
        return watermark;
    }

    public void setWatermark(long watermark) {
        this.watermark = watermark;
    }


    public long getPullIndex() {
        return pullIndex;
    }

    public void setPullIndex(long pullIndex) {
        this.pullIndex = pullIndex;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long increaseVersion() {
        return ++version;
    }
}

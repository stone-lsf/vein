package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.cluster.discovery.gossip.GossipFinishNotifier;
import com.sm.finance.charge.cluster.discovery.gossip.messages.GossipMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:38
 */
public class ReplicateData implements GossipMessage {

    /**
     * 数据的唯一id
     */
    private String id;

    /**
     * 数据的最初来源
     */
    private String from;

    /**
     * 集群版本号
     */
    private long clusterVersion;

    /**
     * 数据索引号
     */
    private long dataIndex;

    /**
     * 实际的数据负载
     */
    private Object payload;

    private GossipFinishNotifier finishNotifier;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(long clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public long getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(long dataIndex) {
        this.dataIndex = dataIndex;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public short getType() {
        return USER;
    }

    @Override
    public boolean invalidate(GossipMessage message) {
        return false;
    }

    @Override
    public void onGossipFinish() {

    }

    @Override
    public void setNotifier(GossipFinishNotifier finishNotifier) {
        this.finishNotifier = finishNotifier;
    }
}

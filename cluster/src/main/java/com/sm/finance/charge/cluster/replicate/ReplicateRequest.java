package com.sm.finance.charge.cluster.replicate;


import com.sm.finance.charge.cluster.storage.Entry;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:38
 */
public class ReplicateRequest {

    /**
     * 数据的最初来源
     */
    private long source;

    /**
     * 数据的目的地
     */
    private long destination;

    /**
     * 集群当前版本号
     */
    private long currentVersion;

    /**
     * 前一次复制的版本号
     */
    private long prevVersion;

    /**
     * 前一次复制的索引号
     */
    private long prevIndex;

    /**
     * 已经提交的数据索引号
     */
    private long commitIndex;

    /**
     * 实际的数据负载
     */
    private List<Entry> entries;

    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public long getPrevVersion() {
        return prevVersion;
    }

    public void setPrevVersion(long prevVersion) {
        this.prevVersion = prevVersion;
    }

    public long getPrevIndex() {
        return prevIndex;
    }

    public void setPrevIndex(long prevIndex) {
        this.prevIndex = prevIndex;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public long getDestination() {
        return destination;
    }

    public void setDestination(long destination) {
        this.destination = destination;
    }

    public long getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(long currentVersion) {
        this.currentVersion = currentVersion;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "ReplicateRequest{" +
            "source=" + source +
            ", destination=" + destination +
            ", currentVersion=" + currentVersion +
            ", prevVersion=" + prevVersion +
            ", prevIndex=" + prevIndex +
            ", commitIndex=" + commitIndex +
            ", entries=" + entries +
            '}';
    }
}

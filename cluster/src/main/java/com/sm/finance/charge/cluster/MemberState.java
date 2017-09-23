package com.sm.finance.charge.cluster;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:11
 */
public class MemberState {

    private final ClusterMember member;

    /**
     * 自增版本号
     */
    private volatile long version;

    /**
     * 下一个需要append的日志的index
     */
    private volatile long nextLogIndex = -1;

    /**
     * 已经提交的日志的index
     */
    private volatile long committedIndex = -1;

    /**
     * 匹配上的index
     */
    private volatile long matchedIndex;

    /**
     * 成员的snapshot的index
     */
    private volatile long snapshotIndex;

    /**
     * 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增）
     */
    private long lastApplied;

    /**
     * 复制数据失败次数
     */
    private volatile long replicateFailureCount;


    public MemberState(ClusterMember member) {
        this.member = member;
    }

    public ClusterMember getMember() {
        return member;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getNextLogIndex() {
        return nextLogIndex;
    }

    public void setNextLogIndex(long nextLogIndex) {
        this.nextLogIndex = nextLogIndex;
    }

    public long getCommittedIndex() {
        return committedIndex;
    }

    public void setCommittedIndex(long committedIndex) {
        this.committedIndex = committedIndex;
    }

    public long getMatchedIndex() {
        return matchedIndex;
    }

    public void setMatchedIndex(long matchedIndex) {
        this.matchedIndex = matchedIndex;
    }

    public long getSnapshotIndex() {
        return snapshotIndex;
    }

    public void setSnapshotIndex(long snapshotIndex) {
        this.snapshotIndex = snapshotIndex;
    }

    public long getReplicateFailureCount() {
        return replicateFailureCount;
    }

    public long incrementReplicateFailureCount() {
        return ++replicateFailureCount;
    }

    public void resetReplicateFailureCount() {
        replicateFailureCount = 0;
    }

    public long getLastApplied() {
        return lastApplied;
    }

    public void setLastApplied(long lastApplied) {
        this.lastApplied = lastApplied;
    }
}

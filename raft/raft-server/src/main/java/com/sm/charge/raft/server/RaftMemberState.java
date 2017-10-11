package com.sm.charge.raft.server;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:11
 */
public class RaftMemberState {

    private final RaftMember member;

    /**
     * 自增版本号
     */
    private volatile long term;

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
     * 需要复制的下一个snapshot的index
     */
    private volatile long nextSnapshotIndex;

    /**
     * 需要复制的下一个snapshot的offset
     */
    private volatile long nextSnapshotOffset;

    /**
     * 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增）
     */
    private volatile long lastApplied;

    /**
     * 复制数据失败次数
     */
    private volatile long replicateFailureCount;


    public RaftMemberState(RaftMember member) {
        this.member = member;
    }

    public RaftMember getMember() {
        return member;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
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

    public long getNextSnapshotIndex() {
        return nextSnapshotIndex;
    }

    public void setNextSnapshotIndex(long nextSnapshotIndex) {
        this.nextSnapshotIndex = nextSnapshotIndex;
    }

    public long getNextSnapshotOffset() {
        return nextSnapshotOffset;
    }

    public void setNextSnapshotOffset(long nextSnapshotOffset) {
        this.nextSnapshotOffset = nextSnapshotOffset;
    }
}

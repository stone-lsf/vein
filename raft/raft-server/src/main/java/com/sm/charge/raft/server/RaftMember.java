package com.sm.charge.raft.server;

import com.sm.charge.raft.server.replicate.Replicator;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.TransportClient;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:11
 */
public class RaftMember {

    private final RaftMemberState state;

    private final long id;

    private final Address address;

    /**
     * 自增版本号
     */
    private volatile long term;

    /**
     * 在当前获得选票的候选人的 Id
     */
    private volatile long votedFor;

    /**
     * 下一个需要append的日志的index
     */
    private volatile long nextLogIndex = -1;

    /**
     * 已经提交的日志的index
     */
    private volatile long commitIndex = -1;

    /**
     * 匹配上的index
     */
    private volatile long matchedIndex;

    /**
     * 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增）
     */
    private volatile long lastApplied;

//    /**
//     * 成员的snapshot的index
//     */
//    private volatile long snapshotIndex;
//

//    /**
//     * 复制数据失败次数
//     */
//    private volatile long replicateFailureCount;


    public RaftMember(TransportClient client, long id, Address address, Replicator replicator) {
        this.id = id;
        this.address = address;
        this.state = new RaftMemberState(client, this, replicator);
    }

    public long getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public RaftMemberState getState() {
        return state;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(long votedFor) {
        this.votedFor = votedFor;
    }

    public long getNextLogIndex() {
        return nextLogIndex;
    }

    public void setNextLogIndex(long nextLogIndex) {
        this.nextLogIndex = nextLogIndex;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public long getMatchedIndex() {
        return matchedIndex;
    }

    public void setMatchedIndex(long matchedIndex) {
        this.matchedIndex = matchedIndex;
    }

    public long getLastApplied() {
        return lastApplied;
    }

    public void setLastApplied(long lastApplied) {
        this.lastApplied = lastApplied;
    }
}

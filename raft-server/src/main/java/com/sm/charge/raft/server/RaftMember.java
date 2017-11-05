package com.sm.charge.raft.server;

import com.sm.charge.raft.server.state.support.Replicator;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.BaseNode;
import com.sm.finance.charge.transport.api.TransportClient;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:11
 */
public class RaftMember extends BaseNode<String>{

    private final RaftMemberState state;

    /**
     * 自增版本号
     */
    private volatile long term;

    /**
     * 在当前获得选票的候选人的 Id
     */
    private volatile String votedFor;

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


    public RaftMember(TransportClient client, String id, Address address, Replicator replicator) {
        super(id,address);
        this.state = new RaftMemberState(client, this, replicator);
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

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
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

    @Override
    public String toString() {
        return "RaftMember{" +
            "nodeId=" + nodeId +
            ", address=" + address +
            ", state=" + state +
            ", term=" + term +
            ", votedFor='" + votedFor + '\'' +
            ", nextLogIndex=" + nextLogIndex +
            ", commitIndex=" + commitIndex +
            ", matchedIndex=" + matchedIndex +
            ", lastApplied=" + lastApplied +
            '}';
    }
}

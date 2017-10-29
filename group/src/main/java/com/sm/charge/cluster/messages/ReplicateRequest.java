package com.sm.charge.cluster.messages;

import com.sm.charge.cluster.group.Entry;
import com.sm.finance.charge.common.Address;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午11:22
 */
public class ReplicateRequest<T> {

    private Address leader;

    /**
     * 已经提交的数据索引号
     */
    private long commitIndex;

    /**
     * 实际的数据负载
     */
    private List<Entry<T>> entries;

    public Address getLeader() {
        return leader;
    }

    public void setLeader(Address leader) {
        this.leader = leader;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public List<Entry<T>> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry<T>> entries) {
        this.entries = entries;
    }
}

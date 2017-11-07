package com.sm.charge.raft.server.events;

import com.sm.charge.raft.client.Event;
import com.sm.charge.raft.server.RaftMember;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:47
 */
public class JoinResponse implements Event {

    public static final int SUCCESS = 0;
    public static final int REDIRECT = 1;
    public static final int RECONFIGURING = 2;
    public static final int NO_LEADER = 3;
    public static final int INTERNAL_ERROR = 3;

    /**
     * 状态码
     */
    private int status;

    private long index;

    private long term;

    private long timestamp;

    /**
     * 集群服务器列表
     */
    private List<RaftMember> members;

    private RaftMember master;

    public boolean isSuccess() {
        return status == SUCCESS;
    }


    public boolean needRedirect() {
        return status == REDIRECT;
    }

    public boolean reconfiguring() {
        return status == RECONFIGURING;
    }

    public boolean noLeader() {
        return status == NO_LEADER;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<RaftMember> getMembers() {
        return members;
    }

    public void setMembers(List<RaftMember> members) {
        this.members = members;
    }

    public RaftMember getMaster() {
        return master;
    }

    public void setMaster(RaftMember master) {
        this.master = master;
    }

    @Override
    public String toString() {
        return "JoinResponse{" +
            "status=" + status +
            ", index=" + index +
            ", term=" + term +
            ", timestamp=" + timestamp +
            ", members=" + members +
            ", master=" + master +
            '}';
    }
}

package com.sm.charge.raft.server.election;

import com.sm.charge.raft.server.RaftMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:47
 */
public class VoteResponse extends RaftMessage {

    /**
     * 是否获得选票
     */
    private boolean voteGranted;

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    @Override
    public String toString() {
        return "VoteResponse{" +
            "term=" + term +
            ", voteGranted=" + voteGranted +
            ", source='" + source + '\'' +
            ", destination='" + destination + '\'' +
            '}';
    }
}

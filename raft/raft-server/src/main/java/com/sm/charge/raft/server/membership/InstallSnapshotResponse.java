package com.sm.charge.raft.server.membership;

import com.sm.charge.raft.server.RaftMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/10/13 下午4:23
 */
public class InstallSnapshotResponse extends RaftMessage {

    /**
     * 是否接受
     */
    private boolean accepted;

    /**
     * 期望的下一个日志Index
     */
    private long nextIndex;

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public long getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(long nextIndex) {
        this.nextIndex = nextIndex;
    }
}

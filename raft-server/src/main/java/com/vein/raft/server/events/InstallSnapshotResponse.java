package com.vein.raft.server.events;

import com.vein.raft.server.RaftMessage;

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
     * 期望的下一个偏移地址
     */
    private long nextOffset;

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public long getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(long nextOffset) {
        this.nextOffset = nextOffset;
    }
}

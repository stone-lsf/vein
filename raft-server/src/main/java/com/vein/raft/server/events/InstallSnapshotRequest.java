package com.vein.raft.server.events;

import com.vein.raft.server.RaftMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/10/13 下午4:23
 */
public class InstallSnapshotRequest extends RaftMessage {
    /**
     * 快照中包含的最后日志条目的索引值
     */
    private long index;

    /**
     * 分块在快照中的偏移量
     */
    private int offset;

    private byte data[];

    /**
     * 如果这是最后一个分块则为 true
     */
    private boolean complete;

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}

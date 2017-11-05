package com.sm.charge.raft.server.storage.logs.index;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午2:14
 */
public class LogIndex {
    public static final int LENGTH = 8 + 8;

    private long index;
    private long offset;

    public LogIndex() {
    }

    public LogIndex(long index, long offset) {
        this.index = index;
        this.offset = offset;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}

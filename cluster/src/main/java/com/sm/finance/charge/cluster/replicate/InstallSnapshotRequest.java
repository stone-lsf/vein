package com.sm.finance.charge.cluster.replicate;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午11:25
 */
public class InstallSnapshotRequest {

    private long version;

    private long index;

    private long from;

    private long offset;

    private boolean complete;

    private byte[] data;


    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

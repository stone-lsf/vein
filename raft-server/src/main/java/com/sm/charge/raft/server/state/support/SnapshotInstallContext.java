package com.sm.charge.raft.server.state.support;

import com.sm.charge.raft.server.storage.snapshot.Snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/10/20 下午5:21
 */
public class SnapshotInstallContext {

    private final Snapshot snapshot;

    private volatile long offset;

    private volatile long size;

    private volatile boolean complete;

    public SnapshotInstallContext(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}

package com.vein.raft.server.state.support;

import com.vein.raft.server.storage.snapshot.Snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/10/20 下午5:21
 */
public class SnapshotInstallContext {

    private final Snapshot snapshot;

    private volatile int offset;

    private volatile int size;

    private volatile boolean complete;

    public SnapshotInstallContext(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}

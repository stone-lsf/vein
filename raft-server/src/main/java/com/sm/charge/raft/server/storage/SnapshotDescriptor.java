package com.sm.charge.raft.server.storage;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午5:10
 */
public class SnapshotDescriptor {
    private final long index;
    private final long timestamp;
    private final Lock lock = new ReentrantLock();

    public SnapshotDescriptor(long index, long timestamp) {
        this.index = index;
        this.timestamp = timestamp;
    }

    public long index() {
        return index;
    }

    public long timestamp() {
        return timestamp;
    }

    void lock() {
        lock.lock();
    }

    void unlock() {
        lock.unlock();
    }
}

package com.sm.finance.charge.cluster.storage.snapshot;

import com.sm.charge.buffer.BufferOutputStream;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午7:36
 */
public interface SnapshotWriter extends BufferOutputStream<SnapshotWriter> {

    SnapshotWriter writeObject(Object object);
}

package com.sm.finance.charge.cluster.storage.snapshot.file;

import com.sm.finance.charge.cluster.storage.snapshot.SnapshotReader;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午10:51
 */
public class FileSnapshotReader implements SnapshotReader{
    @Override
    public long remaining() {
        return 0;
    }

    @Override
    public SnapshotReader read(byte[] bytes) {
        return null;
    }

    @Override
    public boolean hasRemaining() {
        return false;
    }

    @Override
    public void close() throws Exception {

    }
}

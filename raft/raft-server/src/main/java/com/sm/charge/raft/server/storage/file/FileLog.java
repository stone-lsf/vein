package com.sm.charge.raft.server.storage.file;

import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.LogEntry;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午12:21
 */
public class FileLog implements Log {
    @Override
    public long append(LogEntry entry) {
        return 0;
    }

    @Override
    public Log skip(long entries) {
        return null;
    }

    @Override
    public long firstIndex() {
        return 0;
    }

    @Override
    public long lastIndex() {
        return 0;
    }

    @Override
    public LogEntry get(long index) {
        return null;
    }

    @Override
    public LogEntry lastEntry() {
        return null;
    }

    @Override
    public Log truncate(long index) {
        return null;
    }

    @Override
    public Log commit(long index) {
        return null;
    }
}

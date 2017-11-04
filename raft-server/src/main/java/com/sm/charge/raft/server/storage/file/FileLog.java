package com.sm.charge.raft.server.storage.file;

import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.finance.charge.common.LongIdGenerator;
import com.sm.finance.charge.common.base.LoggerSupport;

import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午12:21
 */
public class FileLog extends LoggerSupport implements Log {

    //    private final FileStorage fileStorage;
    private final LongIdGenerator indexGenerator = new LongIdGenerator(0);
    private final ConcurrentNavigableMap<Long, LogEntry> entryBuffer = new ConcurrentSkipListMap<>();
//    private final StorageAppender storageWriter;

    public FileLog() {
//        this.fileStorage = fileStorage;
//        this.storageWriter = fileStorage.appender();
    }

    @Override
    public long append(LogEntry entry) {
        long index = indexGenerator.nextId();
        entry.setIndex(index);
        entryBuffer.put(index, entry);
        return index;
    }

    @Override
    public Log skip(long entries) {
        indexGenerator.skip(entries);
        return this;
    }

    @Override
    public long firstIndex() {
        //TODO get first
        return 0;
    }

    @Override
    public long lastIndex() {
        LogEntry entry = lastEntry();
        if (entry == null) {
            return 0;
        }
        return entry.getIndex();
    }

    @Override
    public LogEntry get(long index) {
        LogEntry entry = entryBuffer.get(index);
        if (entry != null) {
            return entry;
        }

        return null;
    }

    @Override
    public LogEntry lastEntry() {
        Map.Entry<Long, LogEntry> lastEntry = entryBuffer.lastEntry();
        if (lastEntry != null) {
            return lastEntry.getValue();
        }
        return null;
    }

    @Override
    public Log truncate(long index) {
        NavigableSet<Long> set = entryBuffer.keySet();
        for (Long idx : set) {
            if (idx >= index) {
                entryBuffer.remove(idx);
            }
        }
        return this;
    }

    @Override
    public Log commit(long index) {
        NavigableSet<Long> set = entryBuffer.keySet();
        try {
            for (Long idx : set) {
                if (idx > index) {
                    break;
                }
                LogEntry entry = entryBuffer.get(idx);
//                boolean success = storageWriter.append(entry);
                boolean success = false;
                if (success) {
                    entryBuffer.remove(idx);
                }
            }
        } catch (Throwable e) {
            logger.error("commit entry to:{} caught exception", index, e);
            System.exit(-1);
        }
        return this;
    }
}

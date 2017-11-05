package com.sm.charge.raft.server.storage.logs;


import com.sm.charge.raft.server.storage.logs.entry.LogEntry;
import com.sm.charge.raft.server.storage.logs.segment.Segment;
import com.sm.charge.raft.server.storage.logs.segment.SegmentManager;
import com.sm.finance.charge.common.LongIdGenerator;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.serializer.api.Serializer;

import java.io.File;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午3:40
 */
public class RaftLogger extends LoggerSupport {

    private final File directory;
    private final int maxSegmentSize;
    private final int maxSegmentEntries;
    private final SegmentManager segments;
    private final LongIdGenerator indexGenerator = new LongIdGenerator(0);
    private final ConcurrentNavigableMap<Long, LogEntry> entryBuffer = new ConcurrentSkipListMap<>();


    public RaftLogger(String fileName, File directory, Serializer serializer, int maxSegmentSize, int maxSegmentEntries) {
        this.directory = directory;
        this.maxSegmentSize = maxSegmentSize;
        this.maxSegmentEntries = maxSegmentEntries;
        this.segments = new SegmentManager(fileName, directory, serializer, maxSegmentSize, maxSegmentEntries);
    }

    /**
     * 添加日志
     *
     * @param entry 日志记录
     * @return 返回该日志记录的index
     */
    public long append(LogEntry entry) {
        long index = segments.addEntry(entry);
        entryBuffer.put(index, entry);
        return index;
    }

    /**
     * 将索引id生成器跳过指定的数量
     *
     * @param entries 需要跳过的数量
     * @return {@link RaftLogger}
     */
    public RaftLogger skip(long entries) {
        indexGenerator.skip(entries);
        return this;
    }

    /**
     * 返回第一条日志记录的index
     *
     * @return 如果有日志记录，则返回第一条日志记录的index，否则返回0
     */
    public long firstIndex() {
        //TODO get first
        return 0;
    }

    /**
     * 返回最后一条日志记录的index
     *
     * @return 如果有日志记录，则返回最后一条日志记录的index，否则返回0
     */
    public long lastIndex() {
        LogEntry entry = lastEntry();
        if (entry == null) {
            return 0;
        }
        return entry.getIndex();
    }

    /**
     * 获取指定index的日志记录
     *
     * @param index log index
     * @return {@link LogEntry}
     */
    public LogEntry get(long index) {
        LogEntry entry = entryBuffer.get(index);
        if (entry != null) {
            return entry;
        }

        Segment segment = segments.segment(index);
        if (segment == null) {
            return null;
        }

        return segment.get(index);
    }

    /**
     * 最后一条记录
     *
     * @return {@link LogEntry}
     */
    public LogEntry lastEntry() {
        Map.Entry<Long, LogEntry> lastEntry = entryBuffer.lastEntry();
        if (lastEntry != null) {
            return lastEntry.getValue();
        }
        return null;
    }

    /**
     * truncate所有大于index的日志
     *
     * @param index 指定日志的索引
     */
    public RaftLogger truncate(long index) {
        NavigableSet<Long> set = entryBuffer.keySet();
        for (Long idx : set) {
            if (idx >= index) {
                entryBuffer.remove(idx);
            }
        }
        return this;
    }

    /**
     * 提交所有小于等于index的日志
     *
     * @param index log index
     * @return {@link RaftLogger}
     */
    public RaftLogger commit(long index) {
        segments.flush();
        return this;
    }

    /**
     * 返回存储目录
     *
     * @return 目录
     */
    public File directory() {
        return directory;
    }

    /**
     * 最大segment大小(字节)
     *
     * @return 字节数
     */
    public int maxSegmentSize() {
        return maxSegmentSize;
    }

    /**
     * 最大segment大小(字节)
     *
     * @return 字节数
     */
    public int maxSegmentEntries() {
        return maxSegmentEntries;
    }
}

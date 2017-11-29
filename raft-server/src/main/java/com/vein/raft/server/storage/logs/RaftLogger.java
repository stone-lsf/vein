package com.vein.raft.server.storage.logs;


import com.vein.raft.server.storage.logs.entry.LogEntry;
import com.vein.raft.server.storage.logs.index.LogIndex;
import com.vein.raft.server.storage.logs.segment.Segment;
import com.vein.raft.server.storage.logs.segment.SegmentManager;
import com.vein.common.NamedThreadFactory;
import com.vein.common.base.LoggerSupport;
import com.vein.serializer.api.Serializer;

import java.io.File;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午3:40
 */
public class RaftLogger extends LoggerSupport {

    private final File directory;
    private final int maxSegmentSize;
    private final int maxMessageSize;
    private final int maxSegmentEntries;
    private final SegmentManager segments;
    private final ConcurrentNavigableMap<Long, LogEntry> entryBuffer = new ConcurrentSkipListMap<>();
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("LogCleanPool"));

    public RaftLogger(String fileName, File directory, Serializer serializer, int maxSegmentSize, int maxMessageSize, int maxSegmentEntries) {
        this.directory = directory;
        this.maxSegmentSize = maxSegmentSize;
        this.maxMessageSize = maxMessageSize;
        this.maxSegmentEntries = maxSegmentEntries;
        this.segments = new SegmentManager(fileName, directory, serializer, maxSegmentSize, maxSegmentEntries, maxMessageSize);
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
     */
    public void skip(long entries) {
        segments.skip(entries);
    }

    /**
     * 返回第一条日志记录的index
     *
     * @return 如果有日志记录，则返回第一条日志记录的index，否则返回0
     */
    public long firstIndex() {
        Segment segment = segments.firstSegment();
        if (segment == null) {
            return 0;
        }

        if (!segment.isOpened()) {
            segment.open();
        }

        LogIndex logIndex = segment.firstIndex();
        if (logIndex != null) {
            return logIndex.getIndex();
        }

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
        if (!segment.isOpened()) {
            segment.open();
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

        Segment segment = segments.currentSegment();
        if (segment == null) {
            return null;
        }

        if (!segment.isOpened()) {
            segment.open();
        }

        LogIndex logIndex = segment.lastIndex();
        if (logIndex != null && logIndex.getIndex() > 0) {
            return segment.get(logIndex.getIndex());
        }

        return null;
    }

    /**
     * truncate所有大于index的日志
     *
     * @param index 指定日志的索引
     */
    public void truncate(long index) {
        NavigableSet<Long> set = entryBuffer.keySet();
        for (Long idx : set) {
            if (idx >= index) {
                entryBuffer.remove(idx);
            }
        }
    }

    /**
     * 提交所有小于等于index的日志
     *
     * @param index log index
     */
    public void commit(long index) {
        segments.flush();
        executorService.schedule(() -> {
            Long startIndex = entryBuffer.firstKey();
            while (startIndex <= index) {
                entryBuffer.remove(startIndex);
                startIndex++;
            }
        }, 10 * 1000, TimeUnit.MILLISECONDS);
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

    /**
     * 最大segment大小(字节)
     *
     * @return 字节数
     */
    public int maxMessageSize() {
        return maxMessageSize;
    }
}

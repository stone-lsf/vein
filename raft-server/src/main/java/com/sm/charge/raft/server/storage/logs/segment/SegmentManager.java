package com.sm.charge.raft.server.storage.logs.segment;

import com.sm.charge.raft.server.storage.logs.entry.LogEntry;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.common.utils.FileUtil;
import com.sm.finance.charge.serializer.api.Serializer;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 上午11:52
 */
public class SegmentManager extends LoggerSupport implements FileNameRule {
    static final String EXTENSION = "segment";
    private static final char EXTENSION_SEPARATOR = '.';
    private static final char SEPARATOR = '_';

    private final String fileName;
    private final File directory;
    private final Serializer serializer;
    private final int maxSegmentSize;
    private final int maxSegmentEntries;

    private final ConcurrentNavigableMap<Long, Segment> segments = new ConcurrentSkipListMap<>();
    private volatile Segment currentSegment;

    public SegmentManager(String fileName, File directory, Serializer serializer, int maxSegmentSize, int maxSegmentEntries) {
        this.fileName = fileName;
        this.directory = directory;
        this.serializer = serializer;
        this.maxSegmentSize = maxSegmentSize;
        this.maxSegmentEntries = maxSegmentEntries;
        loadSegments();
        check();
    }

    private void loadSegments() {
        Collection<File> files;
        try {
            files = FileUtil.listAllFile(directory, File::isFile);
        } catch (Exception e) {
            logger.error("load directory:{} files caught exception", directory, e);
            throw new IllegalStateException(e);
        }

        for (File file : files) {
            long baseIndex = parse(file.getName());
            if (baseIndex == -1) {
                continue;
            }

            Segment segment = new Segment(file, baseIndex, serializer);
            segments.put(baseIndex, segment);
        }

        Map.Entry<Long, Segment> lastEntry = segments.lastEntry();
        if (lastEntry != null) {
            currentSegment = lastEntry.getValue();
        }
    }

    private void check() {
        Map.Entry<Long, Segment> lastEntry = segments.lastEntry();
        if (lastEntry == null) {
            Segment segment = nextSegment(1);
            segments.put(1L, segment);
            currentSegment = segment;
        }

        for (Long index : segments.keySet()) {
            Segment segment = segments.get(index);
            if (segment != currentSegment) {
                segment.buildIndex(maxSegmentEntries);
            } else {
                segment.rebuildIndex(maxSegmentEntries);
            }
        }
    }


    public long addEntry(LogEntry entry) {
        long index = currentSegment.append(entry);
        if (needRolling(currentSegment)) {
            currentSegment = nextSegment(index + 1);
        }
        return index;
    }

    public Segment segment(long index) {
        Map.Entry<Long, Segment> segment = segments.floorEntry(index);
        return segment != null ? segment.getValue() : null;
    }

    public void flush() {
        currentSegment.flush();
    }

    private Segment nextSegment(long index) {
        File file = new File(directory, generate(index));

        Segment segment = new Segment(file, index, serializer);
        segments.put(index, segment);
        if (currentSegment != null) {
            currentSegment.flush();
            currentSegment.close();
        }

        return segment;
    }

    private boolean needRolling(Segment segment) {
        return segment.size() >= maxSegmentSize || segment.entries() >= maxSegmentEntries;
    }

    @Override
    public String generate(long startIndex) {
        return fileName + SEPARATOR + startIndex + EXTENSION_SEPARATOR + EXTENSION;
    }

    @Override
    public long parse(String file) {
        if (!file.startsWith(fileName)) {
            return -1;
        }

        if (!file.endsWith(EXTENSION_SEPARATOR + EXTENSION)) {
            return -1;
        }

        int end = file.lastIndexOf(EXTENSION_SEPARATOR);
        int start = file.lastIndexOf(SEPARATOR);
        if (start == -1) {
            return -1;
        }

        String index = file.substring(start + 1, end);
        try {
            long value = Long.parseLong(index);
            if (value <= 0) {
                return -1;
            }
            return value;
        } catch (Throwable e) {
            logger.warn("file:{} is not a segment", file);
            return -1;
        }
    }
}

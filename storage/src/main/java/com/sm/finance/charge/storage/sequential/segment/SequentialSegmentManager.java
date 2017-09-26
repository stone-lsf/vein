package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.FileUtil;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentDescriptor;
import com.sm.finance.charge.storage.api.segment.SegmentManager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentManager extends LogSupport implements SegmentManager {
    private static final String EXTENSION = "segment";
    private static final char EXTENSION_SEPARATOR = '.';

    private final File directory;
    private ConcurrentNavigableMap<Long, Segment> segmentMap = new ConcurrentSkipListMap<>();

    public SequentialSegmentManager(File directory) {
        this.directory = directory;
    }

    @Override
    public void start() throws Exception {
        loadSegments();
        Map.Entry<Long, Segment> entry = segmentMap.lastEntry();
        if (entry == null) {
            return;
        }

        Segment segment = entry.getValue();
        long validOffset = segment.check();
        segment.truncate(validOffset);
    }

    private void loadSegments() throws IOException {
        Collection<File> files = FileUtil.listAllFile(directory, File::isFile);

        for (File file : files) {
            SegmentDescriptor descriptor = parseDescriptor(file);
            if (descriptor == null) {
                continue;
            }

            SequentialSegment segment = new SequentialSegment(descriptor, file);
            segmentMap.put(descriptor.sequence(), segment);
        }
    }


    private SegmentDescriptor parseDescriptor(File file) {
        String name = file.getName();
        if (!name.endsWith(EXTENSION_SEPARATOR + EXTENSION)) {
            return null;
        }

        int end = name.lastIndexOf(EXTENSION_SEPARATOR);
        if (end < 0) {
            return null;
        }

        String sequence = name.substring(0, end);
        long value;
        try {
            value = Long.parseLong(sequence);
        } catch (Throwable e) {
            logger.warn("file:{} is not a segment", name);
            return null;
        }

        if (value <= 0) {
            return null;
        }

        return new SequentialSegmentDescriptor(value);
    }

    @Override
    public Segment create(long sequence) {
        SegmentDescriptor descriptor = new SequentialSegmentDescriptor(sequence);
        File file = buildSegmentFile(sequence);

        Segment segment = new SequentialSegment(descriptor, file);
        segmentMap.put(sequence, segment);
        return segment;
    }


    private File buildSegmentFile(long sequence) {
        String name = sequence + EXTENSION_SEPARATOR + EXTENSION;
        return new File(directory, name);
    }

    @Override
    public void close() throws Exception {

    }


}

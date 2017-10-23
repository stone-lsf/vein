package com.sm.finance.charge.storage.sequential.segment;

import com.google.common.collect.Lists;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.utils.FileUtil;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentDescriptor;
import com.sm.finance.charge.storage.api.segment.SegmentManager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentManager extends AbstractService implements SegmentManager {
    private static final String EXTENSION = "segment";
    private static final char EXTENSION_SEPARATOR = '.';

    private final File directory;

    private ConcurrentNavigableMap<Long, Segment> segmentMap = new ConcurrentSkipListMap<>();

    public SequentialSegmentManager(File directory) {
        this.directory = directory;

    }

    @Override
    protected void doStart() throws Exception {
        loadSegments();
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
    public Segment create(long sequence) throws IOException {
        checkStarted();
        SegmentDescriptor descriptor = new SequentialSegmentDescriptor(sequence);
        File file = buildSegmentFile(sequence);

        Segment segment = new SequentialSegment(descriptor, file);
        segmentMap.put(sequence, segment);
        return segment;
    }

    @Override
    public boolean delete(long sequence) {
        checkStarted();
        Segment segment = segmentMap.remove(sequence);
        return segment == null || segment.delete();
    }

    @Override
    public Segment get(long sequence) {
        checkStarted();
        return segmentMap.get(sequence);
    }

    @Override
    public Segment last() {
        checkStarted();
        Map.Entry<Long, Segment> entry = segmentMap.lastEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    @Override
    public List<Segment> higher(long sequence) {
        ConcurrentNavigableMap<Long, Segment> tailMap = segmentMap.tailMap(sequence, true);
        return Lists.newArrayList(tailMap.values());
    }


    private File buildSegmentFile(long sequence) {
        String name = sequence + EXTENSION_SEPARATOR + EXTENSION;
        return new File(directory, name);
    }

    @Override
    protected void doClose() {

    }

}

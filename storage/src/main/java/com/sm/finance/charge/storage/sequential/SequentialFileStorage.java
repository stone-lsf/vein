package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.IoUtil;
import com.sm.finance.charge.storage.api.FileStorage;
import com.sm.finance.charge.storage.api.StorageConfig;
import com.sm.finance.charge.storage.api.StorageReader;
import com.sm.finance.charge.storage.api.StorageWriter;
import com.sm.finance.charge.storage.api.index.IndexAppender;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.IndexManager;
import com.sm.finance.charge.storage.api.index.OffsetIndex;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentManager;
import com.sm.finance.charge.storage.api.segment.SegmentReader;
import com.sm.finance.charge.storage.sequential.index.SequentialIndexManager;
import com.sm.finance.charge.storage.sequential.index.SequentialOffsetIndex;
import com.sm.finance.charge.storage.sequential.segment.SequentialSegmentManager;

import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:52
 */
public class SequentialFileStorage extends AbstractService implements FileStorage {

    private final File directory;
    private final StorageConfig storageConfig;
    private final SegmentManager segmentManager;
    private final IndexManager indexManager;

    public SequentialFileStorage(File directory, StorageConfig storageConfig) {
        this.directory = directory;
        this.storageConfig = storageConfig;
        this.segmentManager = new SequentialSegmentManager(directory);
        this.indexManager = new SequentialIndexManager(directory);
    }

    @Override
    public File directory() {
        return directory;
    }

    @Override
    public int maxSegmentSize() {
        return storageConfig.getMaxSegmentSize();
    }

    @Override
    public int maxMessageSize() {
        return storageConfig.getMaxMessageSize();
    }

    @Override
    public StorageWriter appender() {
        checkStarted();
        return null;
    }

    @Override
    public StorageReader reader() {
        checkStarted();
        return null;
    }


    @Override
    protected void doStart() throws Exception {
        segmentManager.start();
        indexManager.start();
        recovery();
    }

    private void recovery() throws IOException {
        Segment segment = segmentManager.last();
        if (segment == null) {
            return;
        }

        long sequence = segment.descriptor().sequence();
        IndexFile indexFile = indexManager.last();

        long indexSequence = 0;
        if (indexFile != null) {
            indexSequence = indexFile.firstSequence();
        }

        if (sequence == indexSequence) {
            if (indexFile == null) {
                indexFile = indexManager.create(sequence);
            }
            replenishIndex(segment, indexFile);
        } else {
            replenishIndexFrom(indexSequence);
        }
    }

    /**
     * 补充索引，此时索引未能跟上record
     *
     * @param sequence 最后一个索引文件的sequence
     */
    private void replenishIndexFrom(long sequence) throws IOException {
        IndexFile indexFile = indexManager.get(sequence);
        if (indexFile == null) {
            indexFile = indexManager.create(sequence);
        }

        List<Segment> segments = segmentManager.higher(sequence);
        if (CollectionUtils.isEmpty(segments)) {
            return;
        }

        for (Segment segment : segments) {
            long segmentSequence = segment.descriptor().sequence();
            if (segmentSequence != indexFile.firstSequence()) {
                indexFile = indexManager.create(segmentSequence);
            }
            replenishIndex(segment, indexFile);
        }
    }

    /**
     * 补充指定record文件的索引
     *
     * @param segment   record文件
     * @param indexFile record文件对应的索引文件
     */
    private void replenishIndex(Segment segment, IndexFile indexFile) throws IOException {
        SegmentReader segmentReader = segment.reader();
        OffsetIndex index = indexFile.lastIndex();
        long offset = 0;
        if (index != null) {
            offset = index.offset();
            segmentReader.skip(offset);
        }

        IndexAppender indexAppender = indexFile.appender();
        int interval = storageConfig.getIndexInterval();
        int preIndexed = 1;
        int readCount = 0;

        Entry entry;
        while ((entry = segmentReader.readEntry()) != null) {
            readCount++;
            if (readCount - preIndexed == interval) {
                long sequence = entry.head().sequence();
                SequentialOffsetIndex offsetIndex = new SequentialOffsetIndex(sequence, offset);
                indexAppender.write(offsetIndex);
                preIndexed = readCount;
            }
            offset += entry.size();
        }

        indexAppender.flush();
        IoUtil.close(indexAppender);
        IoUtil.close(segmentReader);
    }

    @Override
    protected void doClose() {

    }
}

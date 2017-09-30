package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.exceptions.BadDiskException;
import com.sm.finance.charge.storage.api.ExceptionHandler;
import com.sm.finance.charge.storage.api.FileStorage;
import com.sm.finance.charge.storage.api.StorageConfig;
import com.sm.finance.charge.storage.api.StorageReader;
import com.sm.finance.charge.storage.api.StorageWriter;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.IndexFileManager;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentManager;
import com.sm.finance.charge.storage.sequential.index.SequentialIndexFileManager;
import com.sm.finance.charge.storage.sequential.segment.SequentialSegmentManager;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:52
 */
public class SequentialFileStorage extends AbstractService implements FileStorage {

    private final File directory;
    private final StorageConfig storageConfig;
    private final SegmentManager segmentManager;
    private final IndexFileManager indexManager;

    public SequentialFileStorage(File directory, StorageConfig config) {
        this.directory = directory;
        this.storageConfig = config;
        this.segmentManager = new SequentialSegmentManager(directory);
        this.indexManager = new SequentialIndexFileManager(directory, config.getIndexInterval(), config.getMaxIndexFileSize());
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
    public void setExceptionHandler(ExceptionHandler handler) {

    }


    @Override
    protected void doStart() throws Exception {
        segmentManager.start();
        indexManager.start();
        recovery();
    }

    /**
     * 恢复索引文件，并返回最后一条entry的sequence
     *
     * @return 最后一条entry的sequence
     * @throws BadDiskException IO异常
     */
    private long recovery() throws BadDiskException {
        Segment segment = segmentManager.last();
        if (segment == null) {
            return 0;
        }

        long sequence = segment.descriptor().sequence();
        IndexFile indexFile = indexManager.last();

        if (indexFile == null) {
            indexFile = indexManager.create(sequence);
            segment.setEntryListener(indexFile::receiveEntry);
        }
        return replenishIndex(segment, indexFile);
    }

    /**
     * 补充指定record文件的索引
     *
     * @param segment   record文件
     * @param indexFile record文件对应的索引文件
     */
    private long replenishIndex(Segment segment, IndexFile indexFile) {
        Pair<Long, Long> pair = segment.check();
        segment.truncate(pair.getRight());
        indexFile.flush();

        return pair.getLeft();
    }

    @Override
    protected void doClose() {

    }
}

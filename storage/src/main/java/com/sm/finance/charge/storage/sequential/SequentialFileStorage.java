package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.serializer.api.Serializer;
import com.sm.finance.charge.storage.api.FileStorage;
import com.sm.finance.charge.storage.api.StorageAppender;
import com.sm.finance.charge.storage.api.StorageConfig;
import com.sm.finance.charge.storage.api.StorageReader;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.IndexFileManager;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentManager;
import com.sm.finance.charge.storage.sequential.index.SequentialIndexFileManager;
import com.sm.finance.charge.storage.sequential.segment.SequentialSegmentManager;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:52
 */
public class SequentialFileStorage extends AbstractService implements FileStorage {

    private final File directory;
    private final StorageConfig storageConfig;
    private final SegmentManager segmentManager;
    private final IndexFileManager indexManager;
    private final Serializer serializer;

    private StorageAppender storageWriter;

    public SequentialFileStorage(File directory, StorageConfig config, Serializer serializer) {
        this.directory = directory;
        this.storageConfig = config;
        this.segmentManager = new SequentialSegmentManager(directory);
        this.serializer = serializer;
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
    public StorageAppender appender() {
        checkStarted();
        return storageWriter;
    }

    @Override
    public StorageReader reader(long startSequence) throws StorageException, BadDataException {
        checkStarted();

        SequentialStorageReader reader = new SequentialStorageReader(indexManager, segmentManager, serializer);
        try {
            reader.readFrom(startSequence);
        } catch (IOException e) {
            logger.error("locate start offset caught exception", e);
            throw new StorageException(e);
        } catch (BadDataException e) {
            logger.error("file has been damage", e);
            throw e;
        }
        return reader;
    }

    @Override
    protected void doStart() throws Exception {
        segmentManager.start();
        indexManager.start();
        long sequence = recovery();
        storageWriter = new SequentialStorageAppender(segmentManager, sequence, indexManager, serializer, storageConfig);
    }

    /**
     * 恢复索引文件，并返回最后一条entry的sequence
     *
     * @return 最后一条entry的sequence
     * @throws IOException IO异常
     */
    private long recovery() throws IOException {
        Segment segment = segmentManager.last();
        if (segment == null) {
            return 0;
        }

        long sequence = segment.baseSequence();
        if (segment.getFile().length() == 0) {
            boolean deleted = segmentManager.delete(sequence);
            if (!deleted) {
                logger.error("delete segment file:{} fail", sequence);
                throw new StorageException("delete segment file:" + sequence + " fail");
            }
            deleted = indexManager.delete(sequence);
            if (!deleted) {
                logger.error("delete index file:{} fail", sequence);
                throw new StorageException("delete index file:" + sequence + " fail");
            }
            segment = segmentManager.last();
            if (segment == null) {
                return 0;
            }
        }


        IndexFile indexFile = indexManager.last();

        if (indexFile == null) {
            indexFile = indexManager.create(sequence);
            segment.setEntryListener(indexFile::receiveEntry);
        }

        Pair<Long, Long> pair = segment.check();
        segment.truncate(pair.getRight());
        indexFile.flush();

        return pair.getLeft();
    }

    @Override
    protected void doClose() {

    }
}

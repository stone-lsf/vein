package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.storage.api.FileStorage;
import com.sm.finance.charge.storage.api.StorageReader;
import com.sm.finance.charge.storage.api.StorageWriter;
import com.sm.finance.charge.common.exceptions.NotStartedException;
import com.sm.finance.charge.storage.api.segment.SegmentManager;
import com.sm.finance.charge.storage.sequential.segment.SequentialSegmentManager;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:52
 */
public class SequentialFileStorage extends AbstractService implements FileStorage {

    private final File directory;
    private final int maxSegmentSize;
    private final int maxMessageSize;
    private final SegmentManager segmentManager;

    public SequentialFileStorage(File directory, int maxSegmentSize, int maxMessageSize) {
        this.directory = directory;
        this.maxSegmentSize = maxSegmentSize;
        this.maxMessageSize = maxMessageSize;
        this.segmentManager = new SequentialSegmentManager(directory);
    }

    @Override
    public File directory() {
        return directory;
    }

    @Override
    public int maxSegmentSize() {
        return maxSegmentSize;
    }

    @Override
    public int maxMessageSize() {
        return maxMessageSize;
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
    }

    @Override
    protected void doClose() {

    }
}

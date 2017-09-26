package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.storage.api.FileStorage;
import com.sm.finance.charge.storage.api.RollingReader;
import com.sm.finance.charge.storage.api.RollingWriter;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:52
 */
public class SequentailFileStorage implements FileStorage {

    private final File directory;
    private final int maxSegmentSize;
    private final int maxMessageSize;

    public SequentailFileStorage(File directory, int maxSegmentSize, int maxMessageSize) {
        this.directory = directory;
        this.maxSegmentSize = maxSegmentSize;
        this.maxMessageSize = maxMessageSize;
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
    public RollingWriter appender() {
        return null;
    }

    @Override
    public RollingReader reader() {
        return null;
    }
}

package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.storage.api.FileStorage;
import com.sm.finance.charge.storage.api.RollingWriter;
import com.sm.finance.charge.storage.api.RollingReader;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:52
 */
public class SequentailFileStorage implements FileStorage {

    private final File directory;
    private final int maxsegmentSize;

    public SequentailFileStorage(File directory, int maxsegmentSize) {
        this.directory = directory;
        this.maxsegmentSize = maxsegmentSize;
    }


    @Override
    public File directory() {
        return directory;
    }

    @Override
    public int maxSegmentSize() {
        return maxsegmentSize;
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

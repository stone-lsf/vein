package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.FileUtil;
import com.sm.finance.charge.common.IoUtil;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.exceptions.BadDiskException;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentAppender;
import com.sm.finance.charge.storage.api.segment.SegmentDescriptor;
import com.sm.finance.charge.storage.api.segment.SegmentReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegment extends LogSupport implements Segment {

    private final File file;
    private final SegmentDescriptor descriptor;


    SequentialSegment(SegmentDescriptor descriptor, File file) {
        this.file = file;
        this.descriptor = descriptor;
    }

    @Override
    public SegmentDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public long check() throws IOException {
        SegmentReader reader = reader();
        long offset = 0;
        Entry entry;
        try {
            while ((entry = reader.readEntry()) != null) {
                entry.check();
                offset += entry.size();
            }
        } catch (BadDataException e) {
            logger.warn("segment:{} has bad data", file.getName());
        } finally {
            IoUtil.close(reader);
        }
        return offset;
    }

    @Override
    public Segment truncate(long offset) {

        try {
            FileUtil.truncate(offset, file);
        } catch (FileNotFoundException e) {
            logger.error("truncate segment file caught exception", e);
            throw new StorageException(e);
        } catch (BadDiskException e) {
            logger.error("truncate segment file caught bad disk exception", e);
            System.exit(-1);
        }
        return this;
    }

    @Override
    public SegmentReader reader() throws IOException {
        return new SequentialSegmentReader(this);
    }

    @Override
    public SegmentAppender appender() throws IOException {
        return new SequentialSegmentAppender(this);
    }


}

package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.IoUtil;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.storage.api.exceptions.BadEntryException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentAppender;
import com.sm.finance.charge.storage.api.segment.SegmentDescriptor;
import com.sm.finance.charge.storage.api.segment.SegmentReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

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
        long offset = reader.position();
        try {
            while (reader.readEntry() != null) {
                offset = reader.position();
            }
        } catch (BadEntryException e) {
            logger.warn("segment:{} has bad data", file.getName());
        } finally {
            IoUtil.close(reader);
        }
        return offset;
    }

    @Override
    public Segment truncate(long offset) {
        RandomAccessFile accessFile;
        try {
            accessFile = new RandomAccessFile(file, "wr");
        } catch (FileNotFoundException e) {
            logger.error("truncate file not found", e);
            throw new RuntimeException(e);
        }
        FileChannel channel = accessFile.getChannel();

        try {
            channel.truncate(offset);
        } catch (IOException e) {
            logger.error("truncate file caught exception", e);
            throw new StorageException(e);
        } finally {
            IoUtil.close(accessFile);
        }

        return this;
    }

    @Override
    public SegmentReader reader() throws IOException {
        return new SequentialSegmentReader(this);
    }

    @Override
    public SegmentAppender appender() {
        return null;
    }


}

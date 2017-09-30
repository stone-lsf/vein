package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.FileUtil;
import com.sm.finance.charge.common.IoUtil;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.exceptions.BadDiskException;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.EntryListener;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentAppender;
import com.sm.finance.charge.storage.api.segment.SegmentDescriptor;
import com.sm.finance.charge.storage.api.segment.SegmentReader;
import com.sm.finance.charge.storage.sequential.ReadBuffer;
import com.sm.finance.charge.storage.sequential.WriterBuffer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
    private EntryListener listener;


    SequentialSegment(SegmentDescriptor descriptor, File file) throws BadDiskException {
        try {
            boolean newFile = file.createNewFile();
            if (newFile) {
                logger.info("create segment file:{}", file);
            }
        } catch (IOException e) {
            throw new BadDiskException(e);
        }
        this.file = file;
        this.descriptor = descriptor;
    }


    @Override
    public void setEntryListener(EntryListener listener) {
        this.listener = listener;
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
    public Pair<Long, Long> check() {
        SegmentReader reader = reader(new ReadBuffer());
        long offset = 0;
        long sequence = descriptor.sequence();
        Entry entry;
        try {
            while ((entry = reader.readEntry()) != null) {
                entry.validCheckSum();
                sequence = entry.head().sequence();

                listener.onCreate(sequence, offset);
                offset += entry.size();
            }
        } catch (BadDataException e) {
            logger.warn("segment:{} has bad data", file.getName());
        } finally {
            IoUtil.close(reader);
        }
        return new ImmutablePair<>(sequence, offset);
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
    public SegmentReader reader(ReadBuffer buffer) {
        return new SequentialSegmentReader(this, buffer);
    }

    @Override
    public SegmentAppender appender(WriterBuffer buffer) {
        return new SequentialSegmentAppender(this, buffer, listener);
    }


}

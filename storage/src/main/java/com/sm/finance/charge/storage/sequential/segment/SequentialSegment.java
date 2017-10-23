package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.utils.FileUtil;
import com.sm.finance.charge.common.utils.IoUtil;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
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
import java.io.IOException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegment extends LoggerSupport implements Segment {

    private final File file;
    private final SegmentDescriptor descriptor;
    private EntryListener listener;
    private boolean active;
    private Segment segment;

    SequentialSegment(SegmentDescriptor descriptor, File file) throws IOException {
        boolean newFile = file.createNewFile();
        if (newFile) {
            logger.info("create segment file:{}", file);
        }
        this.file = file;
        this.descriptor = descriptor;
    }


    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
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
    public long baseSequence() {
        return descriptor.sequence();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Pair<Long, Long> check() throws IOException {
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
    public Segment truncate(long offset) throws IOException {

        FileUtil.truncate(offset, file);
        return this;
    }

    @Override
    public SegmentReader reader(ReadBuffer buffer) throws IOException {
        return new SequentialSegmentReader(this, buffer);
    }

    @Override
    public SegmentAppender appender(WriterBuffer buffer) throws IOException {
        return new SequentialSegmentAppender(this, buffer, listener);
    }

    @Override
    public boolean delete() {
        return file.delete();
    }

    @Override
    public void setNext(Segment segment) {
        this.segment = segment;
    }

    @Override
    public Segment getNext() {
        return segment;
    }
}

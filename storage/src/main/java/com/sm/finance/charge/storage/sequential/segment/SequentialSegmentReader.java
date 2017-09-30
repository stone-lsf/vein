package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.exceptions.BadDiskException;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentReader;
import com.sm.finance.charge.storage.sequential.ReadBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentReader extends LogSupport implements SegmentReader {
    private final Segment segment;
    private final FileChannel fileChannel;
    private final ReadBuffer buffer;

    SequentialSegmentReader(Segment segment, ReadBuffer buffer) {
        this.segment = segment;
        this.buffer = buffer;
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(segment.getFile(), "r");
        } catch (FileNotFoundException e) {
            logger.error("can't find segment:{}", segment.getFile());
            throw new IllegalStateException(e);
        }
        this.fileChannel = accessFile.getChannel();
    }

    @Override
    public Segment getSegment() {
        return segment;
    }


    @Override
    public SegmentReader readFrom(long offset) throws BadDiskException {
        try {
            fileChannel.position(offset);
        } catch (IOException e) {
            logger.error("set reader position caught exception", e);
            throw new BadDiskException(e);
        }
        return this;
    }

    @Override
    public Entry readEntry() {
        SequentialEntry entry = new SequentialEntry();
        buffer.get(entry);
        if (!entry.readComplete()) {
            return null;
        }
        return entry;
    }


    @Override
    public void close() throws Exception {
        fileChannel.close();
    }
}

package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.storage.api.exceptions.BadEntryException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentReader;
import com.sm.finance.charge.storage.sequential.Constants;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentReader extends LogSupport implements SegmentReader {

    private final Segment segment;
    private long offset = 0;
    private final FileChannel fileChannel;
    private volatile ByteBuffer implicit = ByteBuffer.allocate(Constants.maxEntrySize * 4);
    private volatile ByteBuffer explicit = ByteBuffer.allocate(Constants.maxEntrySize * 4);

    SequentialSegmentReader(Segment segment) throws IOException {
        this.segment = segment;
        RandomAccessFile accessFile = new RandomAccessFile(segment.getFile(), "r");
        this.fileChannel = accessFile.getChannel();
    }

    @Override
    public Segment getSegment() {
        return segment;
    }

    @Override
    public long remaining() {
        try {
            return fileChannel.size() - offset;
        } catch (IOException e) {
            logger.error("get file size caught exception", e);
            throw new StorageException(e);
        }
    }

    @Override
    public boolean hasRemaining() {
        return remaining() > 0;
    }

    @Override
    public SegmentReader skip(long bytes) {
        position(offset + bytes);
        return this;
    }

    @Override
    public SegmentReader position(long position) {
        try {
            fileChannel.position(position);
            this.offset = position;
        } catch (IOException e) {
            logger.error("set reader position caught exception", e);
            throw new StorageException(e);
        }
        return this;
    }

    @Override
    public long position() {
        return offset;
    }

    @Override
    public Entry readEntry() throws BadEntryException {
        if (!explicit.hasRemaining()) {
            exchangeBuffer();
        }
        explicit.hasRemaining();
        return null;
    }

    private void exchangeBuffer() {
        ByteBuffer tmp = implicit;
        implicit = explicit;
        explicit = tmp;

        implicit.flip();
        explicit.flip();
        read();
    }

    @Override
    public void close() throws Exception {
        fileChannel.close();
    }


    private void read() {
        try {
            int read = fileChannel.read(implicit);
        } catch (IOException e) {
            logger.error("read from file:{} caught exception:{}", segment.getFile(), e);
            System.exit(-1);
        }
    }
}

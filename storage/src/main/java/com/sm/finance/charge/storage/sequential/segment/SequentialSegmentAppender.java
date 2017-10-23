package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.utils.IoUtil;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.storage.api.exceptions.ClosedException;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.EntryListener;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentAppender;
import com.sm.finance.charge.storage.sequential.WriterBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentAppender extends LogSupport implements SegmentAppender {

    private final Segment segment;
    private final FileChannel fileChannel;
    private final WriterBuffer buffer;
    private final EntryListener listener;

    private volatile boolean closed = false;
    private volatile long appendOffset = 0;

    SequentialSegmentAppender(Segment segment, WriterBuffer buffer, EntryListener listener) throws FileNotFoundException {
        this.segment = segment;
        this.buffer = buffer;
        this.listener = listener;
        RandomAccessFile accessFile = new RandomAccessFile(segment.getFile(), "wr");
        this.fileChannel = accessFile.getChannel();
        this.buffer.setFileChannel(fileChannel);
    }

    @Override
    public Segment getSegment() {
        return segment;
    }

    @Override
    public SegmentAppender appendFrom(long offset) throws IOException {
        appendOffset = offset;
        fileChannel.position(appendOffset);
        return this;
    }

    @Override
    public long appendOffset() {
        return appendOffset;
    }

    @Override
    public SegmentAppender flush() throws IOException {
        checkClosed();
        buffer.flush();
        fileChannel.force(false);
        return this;
    }

    @Override
    public CompletableFuture<Boolean> write(Entry entry) {
        checkClosed();
        CompletableFuture<Boolean> future = buffer.put(entry);
        listener.onCreate(entry.head().sequence(), appendOffset);
        appendOffset += entry.size();
        return future;
    }

    private void checkClosed() {
        if (closed) {
            throw new ClosedException(segment.getFile().getName() + " appender has closed!");
        }
    }


    @Override
    public void close() {
        closed = true;
        IoUtil.close(fileChannel);
    }
}

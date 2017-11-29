package com.vein.storage.sequential.segment;

import com.vein.common.utils.IoUtil;
import com.vein.common.base.LoggerSupport;
import com.vein.storage.api.exceptions.ClosedException;
import com.vein.storage.api.segment.Entry;
import com.vein.storage.api.segment.EntryListener;
import com.vein.storage.api.segment.Segment;
import com.vein.storage.api.segment.SegmentAppender;
import com.vein.storage.sequential.WriterBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentAppender extends LoggerSupport implements SegmentAppender {

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

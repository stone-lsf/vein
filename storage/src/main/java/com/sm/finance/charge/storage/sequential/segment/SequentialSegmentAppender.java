package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.IoUtil;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.storage.api.exceptions.ClosedException;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentAppender;
import com.sm.finance.charge.storage.sequential.Constants;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentAppender extends LogSupport implements SegmentAppender {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("AppendPool"));

    private final Segment segment;
    private final FileChannel fileChannel;
    private volatile ByteBuffer implicit = ByteBuffer.allocate(Constants.maxEntrySize * 4);
    private volatile ByteBuffer explicit = ByteBuffer.allocate(Constants.maxEntrySize * 4);

    private volatile boolean closed = false;

    private AtomicBoolean writing = new AtomicBoolean(false);
    private final Object writeComplete = new Object();

    SequentialSegmentAppender(Segment segment) throws IOException {
        this.segment = segment;
        RandomAccessFile accessFile = new RandomAccessFile(segment.getFile(), "wr");
        this.fileChannel = accessFile.getChannel();
    }

    @Override
    public Segment getSegment() {
        return segment;
    }

    @Override
    public SegmentAppender flush() {
        checkClosed();
        try {
            fileChannel.write(explicit);
            explicit.flip();
        } catch (IOException e) {
            logger.error("flush file:{} caught exception:{}", segment.getFile(), e);
            System.exit(-1);
        }
        return this;
    }

    @Override
    public CompletableFuture<Boolean> write(Entry entry) {
        checkClosed();
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        entry.writeTo(explicit);
        while (!entry.writeComplete()) {
            exchangeBuffer();
        }

        return future;
    }


    private void exchangeBuffer() {
        synchronized (writeComplete) {
            while (writing.get()) {
                try {
                    writeComplete.wait();
                } catch (InterruptedException e) {
                    logger.warn("waiting read end is interrupted", e);
                }
            }
            ByteBuffer tmp = implicit;
            implicit = explicit;
            explicit = tmp;

            implicit.flip();
            appendBuffer();
        }
    }

    private void appendBuffer() {
        if (writing.compareAndSet(false, true)) {
            EXECUTOR_SERVICE.execute(() -> {
                try {
                    fileChannel.write(implicit);
                    implicit.flip();
                    writing.set(false);

                    synchronized (writeComplete) {
                        writeComplete.notify();
                    }
                } catch (IOException e) {
                    logger.error("write to file:{} caught exception:{}", segment.getFile(), e);
                    System.exit(-1);
                }
            });
        }
    }


    private void checkClosed() {
        if (closed) {
            throw new ClosedException(segment.getFile().getName() + " appender has closed!");
        }
    }


    @Override
    public void close() throws Exception {
        implicit = null;
        explicit = null;
        closed = true;
        IoUtil.close(fileChannel);
    }
}

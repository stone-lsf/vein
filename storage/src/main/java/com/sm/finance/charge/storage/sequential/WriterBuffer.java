package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.common.exceptions.BadDiskException;
import com.sm.finance.charge.storage.api.ExceptionHandler;
import com.sm.finance.charge.storage.api.segment.ReadWritable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/9/29 上午11:30
 */
public class WriterBuffer extends LogSupport {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("AppendPool"));

    private volatile ByteBufferWrapper implicit = new ByteBufferWrapper();
    private volatile ByteBufferWrapper explicit = new ByteBufferWrapper();
    private volatile FileChannel fileChannel;
    private volatile Future future;

    private AtomicBoolean writing = new AtomicBoolean(false);
    private final Object writeComplete = new Object();
    private final ExceptionHandler handler;

    public WriterBuffer(ExceptionHandler handler, int flushInterval) {
        this.handler = handler;
        future = EXECUTOR_SERVICE.scheduleWithFixedDelay(this::appendBuffer, flushInterval, flushInterval, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<Boolean> put(ReadWritable readWritable) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        while (!readWritable.writeComplete()) {
            explicit.add(readWritable);
            if (!explicit.hasRemaining()) {
                explicit.prepareComplete();
                exchange();
            }
        }
        explicit.add(future);
        return future;
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void flush() throws BadDiskException {
        synchronized (writeComplete) {
            while (writing.get()) {
                try {
                    writeComplete.wait();
                } catch (InterruptedException e) {
                    logger.warn("waiting read end is interrupted", e);
                }
            }
        }

        explicit.prepareComplete();
        explicit.writeTo(fileChannel);
        explicit.writeComplete();
    }

    private void exchange() {
        synchronized (writeComplete) {
            while (writing.get()) {
                try {
                    writeComplete.wait();
                } catch (InterruptedException e) {
                    logger.warn("waiting read end is interrupted", e);
                }
            }

            ByteBufferWrapper tmp = implicit;
            implicit = explicit;
            explicit = tmp;
            EXECUTOR_SERVICE.execute(this::appendBuffer);
        }
    }

    private void appendBuffer() {
        if (writing.compareAndSet(false, true)) {
            try {
                implicit.writeTo(fileChannel);
            } catch (BadDiskException e) {
                handler.onBadDiskException(e);
                if (future != null) {
                    future.cancel(false);
                }
                return;
            }
            implicit.writeComplete();
            writing.set(false);

            synchronized (writeComplete) {
                writeComplete.notify();
            }
        }
    }


    private class ByteBufferWrapper {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.maxEntrySize * 4);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        void add(CompletableFuture<Boolean> future) {
            futures.add(future);
        }

        boolean hasRemaining() {
            return buffer.hasRemaining();
        }

        void add(ReadWritable readWritable) {
            while (buffer.hasRemaining()) {
                readWritable.writeTo(buffer);
                if (readWritable.writeComplete()) {
                    return;
                }
            }
        }

        void writeTo(FileChannel channel) throws BadDiskException {
            try {
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
            } catch (IOException e) {
                logger.error("write to file caught exception", e);
                throw new BadDiskException(e);
            }
        }

        void prepareComplete() {
            buffer.flip();
        }

        void writeComplete() {
            buffer.flip();
            Iterator<CompletableFuture<Boolean>> iterator = futures.iterator();
            while (iterator.hasNext()) {
                CompletableFuture<Boolean> future = iterator.next();
                future.complete(true);
                iterator.remove();
            }
        }
    }
}

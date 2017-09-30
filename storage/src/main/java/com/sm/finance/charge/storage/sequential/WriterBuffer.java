package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.storage.api.segment.ReadWritable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/9/29 上午11:30
 */
public class WriterBuffer extends LogSupport {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("AppendPool"));

    private volatile ByteBufferWrapper implicit = new ByteBufferWrapper();
    private volatile ByteBufferWrapper explicit = new ByteBufferWrapper();
    private volatile FileChannel fileChannel;

    private AtomicBoolean writing = new AtomicBoolean(false);
    private final Object writeComplete = new Object();

    public CompletableFuture<Boolean> add(ReadWritable readWritable) {
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

    public void flush() {
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
            appendBuffer();
        }
    }

    private void appendBuffer() {
        if (writing.compareAndSet(false, true)) {
            EXECUTOR_SERVICE.execute(() -> {
                implicit.writeTo(fileChannel);
                implicit.writeComplete();
                writing.set(false);

                synchronized (writeComplete) {
                    writeComplete.notify();
                }
            });
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

        void writeTo(FileChannel channel) {
            try {
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
            } catch (IOException e) {
                logger.error("write to file caught exception", e);
                System.exit(-1);
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

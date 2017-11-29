package com.vein.storage.sequential;

import com.vein.common.base.LoggerSupport;
import com.vein.common.NamedThreadFactory;
import com.vein.storage.api.segment.ReadWritable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/9/29 上午11:30
 */
public class WriterBuffer extends LoggerSupport {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("AppendPool"));

    private volatile ByteBufferWrapper implicit = new ByteBufferWrapper();
    private volatile ByteBufferWrapper explicit = new ByteBufferWrapper();
    private volatile FileChannel fileChannel;

    private AtomicBoolean writing = new AtomicBoolean(false);
    private final Object writeComplete = new Object();

    public WriterBuffer(int flushInterval) {
        executorService.scheduleWithFixedDelay(this::appendBuffer, flushInterval, flushInterval, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<Boolean> put(ReadWritable readWritable) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        while (!readWritable.writeComplete()) {
            explicit.add(readWritable);
            if (!explicit.hasRemaining()) {
                explicit.productComplete();
                exchange();
            }
        }
        explicit.add(future);
        return future;
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void flush() throws IOException {
        synchronized (writeComplete) {
            while (writing.get()) {
                try {
                    writeComplete.wait();
                } catch (InterruptedException e) {
                    logger.warn("waiting read end is interrupted", e);
                }
            }
        }

        explicit.productComplete();
        explicit.writeTo(fileChannel);
        explicit.consumeComplete();
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
            executorService.execute(this::appendBuffer);
        }
    }

    private void appendBuffer() {
        if (writing.compareAndSet(false, true)) {
            try {
                implicit.writeTo(fileChannel);
                implicit.consumeComplete();
            } catch (IOException e) {
                logger.error("append message caught exception", e);
                implicit.consumeComplete(e);
            }
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

        void writeTo(FileChannel channel) throws IOException {
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        }

        void productComplete() {
            buffer.flip();
        }

        void consumeComplete() {
            buffer.flip();
            Iterator<CompletableFuture<Boolean>> iterator = futures.iterator();
            while (iterator.hasNext()) {
                CompletableFuture<Boolean> future = iterator.next();
                future.complete(true);
                iterator.remove();
            }
        }

        void consumeComplete(IOException e) {
            buffer.clear();
            Iterator<CompletableFuture<Boolean>> iterator = futures.iterator();
            while (iterator.hasNext()) {
                CompletableFuture<Boolean> future = iterator.next();
                future.completeExceptionally(e);
                iterator.remove();
            }
        }
    }
}

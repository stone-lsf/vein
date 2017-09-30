package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.storage.api.segment.ReadWritable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shifeng.luo
 * @version created on 2017/9/29 下午5:20
 */
public class ReadBuffer extends LogSupport {
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("AppendPool"));

    private volatile ByteBufferWrapper implicit = new ByteBufferWrapper();
    private volatile ByteBufferWrapper explicit = new ByteBufferWrapper();
    private volatile FileChannel fileChannel;

    private final Object readComplete = new Object();
    private AtomicBoolean reading = new AtomicBoolean(false);

    public ReadBuffer() {
        explicit.readComplete();
        preRead();
    }

    public void writeTo(ReadWritable readWritable) {
        if (!explicit.hasRemaining()) {
            return;
        }

        while (!readWritable.writeComplete()) {
            explicit.writeTo(readWritable);
            if (!explicit.hasRemaining()) {
                explicit.prepareComplete();
                exchangeBuffer();
            }
        }
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    private void exchangeBuffer() {
        synchronized (readComplete) {
            while (reading.get()) {
                try {
                    readComplete.wait();
                } catch (InterruptedException e) {
                    logger.warn("waiting read end is interrupted", e);
                }
            }

            ByteBufferWrapper tmp = implicit;
            implicit = explicit;
            explicit = tmp;

            preRead();
        }
    }

    private void preRead() {
        if (reading.compareAndSet(false, true)) {
            executorService.execute(() -> {
                implicit.readFrom(fileChannel);
                implicit.readComplete();

                reading.set(false);
                synchronized (readComplete) {
                    readComplete.notify();
                }
            });
        }
    }

    private class ByteBufferWrapper {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.maxEntrySize * 4);

        boolean hasRemaining() {
            return buffer.hasRemaining();
        }

        void writeTo(ReadWritable readWritable) {
            while (buffer.hasRemaining()) {
                readWritable.readFrom(buffer);
                if (readWritable.writeComplete()) {
                    return;
                }
            }
        }

        void readFrom(FileChannel channel) {
            try {
                while (buffer.hasRemaining()) {
                    channel.read(buffer);
                }
            } catch (IOException e) {
                logger.error("write to file caught exception", e);
                System.exit(-1);
            }
        }

        void prepareComplete() {
            buffer.flip();
        }

        void readComplete() {
            buffer.flip();
        }
    }
}

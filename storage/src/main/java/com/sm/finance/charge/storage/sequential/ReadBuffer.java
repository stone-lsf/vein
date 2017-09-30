package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
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
    private volatile IOException exception;

    private final Object readComplete = new Object();
    private AtomicBoolean reading = new AtomicBoolean(false);


    public ReadBuffer() {
        explicit.productComplete();
        preRead();
    }

    public void get(ReadWritable readWritable) throws IOException, BadDataException {
        if (exception != null) {
            throw exception;
        }

        if (!explicit.hasRemaining()) {
            explicit.consumeComplete();
            exchangeBuffer();
            if (!explicit.hasRemaining()) {
                return;
            }
        }

        boolean exchange = false;
        while (!readWritable.writeComplete()) {
            if (!exchange) {
                explicit.get(readWritable);
                if (!explicit.hasRemaining()) {
                    explicit.consumeComplete();
                    exchangeBuffer();
                    exchange = true;
                }
            } else {
                throw new BadDataException();
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
                try {
                    implicit.readFrom(fileChannel);
                    implicit.productComplete();
                } catch (IOException e) {
                    logger.error("read caught exception", e);
                    exception = e;
                    implicit.productCompleteException();
                }

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

        void get(ReadWritable readWritable) {
            while (buffer.hasRemaining()) {
                readWritable.readFrom(buffer);
                if (readWritable.writeComplete()) {
                    return;
                }
            }
        }

        void readFrom(FileChannel channel) throws IOException {
            while (buffer.hasRemaining()) {
                int readCount = channel.read(buffer);
                if (readCount < 0) {
                    break;
                }
            }
        }

        void consumeComplete() {
            buffer.flip();
        }

        void productComplete() {
            buffer.flip();
        }

        void productCompleteException() {
            buffer.reset();
            buffer.flip();
        }
    }
}

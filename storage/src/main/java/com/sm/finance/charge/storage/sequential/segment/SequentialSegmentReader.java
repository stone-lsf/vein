package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentReader;
import com.sm.finance.charge.storage.sequential.Constants;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.sm.finance.charge.common.SystemConstants.PROCESSORS;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentReader extends LogSupport implements SegmentReader {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(PROCESSORS, new NamedThreadFactory("PreReadPool"));

    private final Segment segment;
    private final FileChannel fileChannel;
    private volatile ByteBuffer implicit = ByteBuffer.allocate(Constants.maxEntrySize * 4);
    private volatile ByteBuffer explicit = ByteBuffer.allocate(Constants.maxEntrySize * 4);

    private final Object readComplete = new Object();
    private AtomicBoolean reading = new AtomicBoolean(false);

    SequentialSegmentReader(Segment segment) throws IOException {
        this.segment = segment;
        RandomAccessFile accessFile = new RandomAccessFile(segment.getFile(), "r");
        this.fileChannel = accessFile.getChannel();
        explicit.flip();
    }

    @Override
    public Segment getSegment() {
        return segment;
    }


    @Override
    public SegmentReader readFrom(long offset) {
        try {
            fileChannel.position(offset);
        } catch (IOException e) {
            logger.error("set reader position caught exception", e);
            System.exit(-1);
        }
        return this;
    }

    @Override
    public Entry readEntry() {
        SequentialEntry entry = new SequentialEntry();
        entry.readFrom(explicit);
        while (!entry.readComplete()) {
            exchangeBuffer();
            entry.readFrom(explicit);
        }

        return entry;
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
            ByteBuffer tmp = implicit;
            implicit = explicit;
            explicit = tmp;

            implicit.flip();
            preRead();
        }
    }

    @Override
    public void close() throws Exception {
        fileChannel.close();
    }


    private void preRead() {
        if (reading.compareAndSet(false, true)) {
            EXECUTOR_SERVICE.execute(() -> {
                try {
                    fileChannel.read(implicit);
                    implicit.flip();
                    reading.set(false);
                    synchronized (readComplete) {
                        readComplete.notify();
                    }
                } catch (IOException e) {
                    logger.error("read from file:{} caught exception:{}", segment.getFile(), e);
                    System.exit(-1);
                }
            });
        }
    }
}

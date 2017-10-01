package com.sm.finance.charge.storage.sequential;

import com.google.common.collect.Lists;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.storage.api.StorageReader;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.IndexFileManager;
import com.sm.finance.charge.storage.api.index.OffsetIndex;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentManager;
import com.sm.finance.charge.storage.api.segment.SegmentReader;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:41
 */
public class SequentialStorageReader extends AbstractService implements StorageReader {
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("AsyncWritePool"));
    private volatile Future<?> future;

    private final IndexFileManager indexFileManager;
    private final SegmentManager segmentManager;
    private final ReadBuffer buffer;
    private volatile SegmentReader reader;

    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>(1000 * 1000);

    SequentialStorageReader(IndexFileManager indexFileManager, SegmentManager segmentManager) {
        this.indexFileManager = indexFileManager;
        this.segmentManager = segmentManager;
        this.buffer = new ReadBuffer();
    }

    @Override
    public void readFrom(long sequence) throws IOException, BadDataException {
        IndexFile indexFile = indexFileManager.lookup(sequence);
        if (indexFile == null) {
            logger.error("can't find start sequence:{} index file", sequence);
            throw new IllegalArgumentException("can't find start sequence:" + sequence + " index file");
        }

        OffsetIndex offsetIndex = indexFile.lookup(sequence);
        if (offsetIndex == null) {
            logger.error("can't find offset index in index file for sequence:{}", sequence);
            throw new IllegalStateException("can't find offset index in index file for sequence:" + sequence);
        }

        Segment segment = segmentManager.get(indexFile.baseSequence());
        reader = segment.reader(buffer);
        reader.readFrom(offsetIndex.offset());

        Entry entry = reader.readEntry();
        while (entry != null && entry.head().sequence() < sequence) {
            entry = reader.readEntry();
        }

        if (entry == null) {
            logger.error("sequence:{} out of range", sequence);
            throw new IllegalArgumentException("sequence:" + sequence + " out of range");
        }

        queue.offer(new Message(entry));
    }

    @Override
    public Object read() {
        return queue.poll();
    }

    @Override
    public List read(int expectCount) {
        List<Object> result = Lists.newArrayListWithCapacity(expectCount);
        for (int i = 0; i < expectCount; i++) {
            Message message = queue.poll();
            if (message == null) {
                break;
            }
            result.add(message);
        }
        return result;
    }

    @Override
    protected void doStart() throws Exception {
        this.future = executorService.submit(this::readAsync);
    }

    @Override
    protected void doClose() {
        future.cancel(false);
    }


    private void readAsync() {
        while (started.get()) {
            Entry entry = null;
            Throwable error = null;
            try {
                entry = reader.readEntry();
            } catch (IOException | BadDataException e) {
                error = e;
            }

            if (error == null) {
                Segment segment = reader.getSegment();
                if (entry == null && !segment.isActive()) {
                    try {
                        reader = segment.getNext().reader(buffer);
                    } catch (IOException e) {
                        error = e;
                    }
                }
            }

            Message message = new Message(entry, error);
            while (true) {
                try {
                    queue.put(message);
                    break;
                } catch (InterruptedException e) {
                    logger.warn("put message interrupt");
                }
            }
        }
    }


    private class Message {
        private Entry data;
        private Throwable error;

        public Message(Entry data) {
            this.data = data;
        }

        public Message(Entry data, Throwable error) {
            this.data = data;
            this.error = error;
        }
    }
}

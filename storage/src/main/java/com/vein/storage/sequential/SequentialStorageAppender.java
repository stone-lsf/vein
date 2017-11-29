package com.vein.storage.sequential;

import com.vein.common.AbstractService;
import com.vein.common.LongIdGenerator;
import com.vein.common.NamedThreadFactory;
import com.vein.serializer.api.Serializable;
import com.vein.serializer.api.Serializer;
import com.vein.storage.api.StorageConfig;
import com.vein.storage.api.StorageAppender;
import com.vein.storage.api.exceptions.StorageException;
import com.vein.storage.api.index.IndexFile;
import com.vein.storage.api.index.IndexFileManager;
import com.vein.storage.api.rolling.Event;
import com.vein.storage.api.rolling.RollingPolicy;
import com.vein.storage.api.segment.Segment;
import com.vein.storage.api.segment.SegmentAppender;
import com.vein.storage.api.segment.SegmentDescriptor;
import com.vein.storage.api.segment.SegmentManager;
import com.vein.storage.sequential.rolling.TimeSizeEvent;
import com.vein.storage.sequential.segment.SequentialEntry;
import com.vein.storage.sequential.segment.SequentialHeader;
import com.vein.storage.sequential.segment.SequentialSegmentDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static com.vein.storage.api.Compress.NONE;
import static com.vein.storage.sequential.segment.SequentialHeader.FIXED_LENGTH;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:41
 */
public class SequentialStorageAppender extends AbstractService implements StorageAppender, RollingPolicy {
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("AsyncWritePool"));
    private volatile Future<?> future;

    private final SegmentManager segmentManager;
    private final IndexFileManager indexFileManager;
    private final LongIdGenerator sequenceGenerator;
    private final Serializer serializer;
    private final WriterBuffer writerBuffer;
    private final int maxSegmentSize;

    private volatile SegmentAppender appender;
    private BlockingQueue<Message> blockingQueue = new LinkedBlockingQueue<>(1000 * 1000);
    private ConcurrentMap<Long, CompletableFuture<Boolean>> futureMap = new ConcurrentHashMap<>(1000 * 1000);


    SequentialStorageAppender(SegmentManager segmentManager, long startSequence, IndexFileManager indexFileManager,
                              Serializer serializer, StorageConfig config) {
        this.segmentManager = segmentManager;
        this.sequenceGenerator = new LongIdGenerator(startSequence);
        this.indexFileManager = indexFileManager;
        this.serializer = serializer;
        this.maxSegmentSize = config.getMaxSegmentSize();
        this.writerBuffer = new WriterBuffer(config.getFlushInterval());

    }

    @Override
    protected void doStart() throws Exception {
        Segment segment = segmentManager.last();
        if (segment == null) {
            segment = segmentManager.create(1);
        }
        this.appender = segment.appender(writerBuffer);
        segment.setActive(true);
        this.future = executorService.submit(this::writeAsync);
    }

    @Override
    public boolean append(Serializable message) {
        CompletableFuture<Boolean> future = appendAsync(message);
        try {
            future.join();
            return true;
        } catch (CompletionException e) {
            throw new StorageException(e.getCause());
        }
    }

    @Override
    public boolean append(List<? extends Serializable> messages) {
        CompletableFuture<Boolean> future = appendAsync(messages);
        try {
            future.join();
            return true;
        } catch (CompletionException e) {
            throw new StorageException(e.getCause());
        }
    }

    @Override
    public boolean appendForce(Serializable message) {
        boolean success = append(message);
        if (success) {
            flush();
        }
        return success;
    }

    @Override
    public boolean appendForce(List<? extends Serializable> messages) {
        boolean success = append(messages);
        if (success) {
            flush();
        }
        return success;
    }

    private void flush() {
        try {
            appender.flush();
        } catch (IOException e) {
            logger.error("flush appender:{} caught exception:{}", appender, e);
            throw new StorageException(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> appendAsync(Serializable message) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        byte[] bytes = serializer.serialize(message);
        Message msg = new Message(bytes, future);

        try {
            blockingQueue.put(msg);
        } catch (InterruptedException e) {
            logger.warn("put message is interrupted", e);
            return CompletableFuture.completedFuture(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> appendAsync(List<? extends Serializable> messages) {
        List<Message> list = new ArrayList<>(messages.size());
        int length = 0;
        for (Serializable message : messages) {
            byte[] bytes = serializer.serialize(message);
            Message msg = new Message(bytes);
            list.add(msg);
            length += bytes.length;
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        BatchMessage msg = new BatchMessage(list, future);
        msg.length = length;

        try {
            blockingQueue.put(msg);
        } catch (InterruptedException e) {
            logger.warn("put message is interrupted", e);
            return CompletableFuture.completedFuture(false);
        }
        return future;
    }


    @Override
    protected void doClose() {
        future.cancel(false);
        executorService.shutdown();
    }

    private void writeAsync() {
        while (started.get()) {
            Message message;
            try {
                message = blockingQueue.take();
            } catch (InterruptedException e) {
                logger.warn("take message interrupted", e);
                continue;
            }

            long sequence = sequenceGenerator.nextId();
            SequentialEntry entry = createEntry(sequence, message);

            long offset = appender.appendOffset();
            TimeSizeEvent event = new TimeSizeEvent(offset + entry.size(), new Date());
            if (isTriggerEvent(event)) {
                try {
                    rollover();
                    Segment segment = nextSegment(new SequentialSegmentDescriptor(sequence));
                    this.appender = segment.appender(writerBuffer);
                } catch (IOException e) {
                    logger.error("create segment caught exception", e);
                    CompletableFuture future = futureMap.remove(sequence);
                    if (future != null) {
                        future.completeExceptionally(new StorageException(e));
                    }
                    break;
                }
            }

            appender.write(entry).whenComplete((result, error) -> {
                CompletableFuture<Boolean> future = futureMap.remove(sequence);
                if (error != null) {
                    logger.error("append message caught exception", error);
                    if (future != null) {
                        future.completeExceptionally(new StorageException(error));
                    }
                } else {
                    if (future != null) {
                        future.complete(result);
                    }
                }
            });
        }
    }


    private SequentialEntry createEntry(long sequence, Message message) {
        SequentialHeader header = new SequentialHeader();
        header.setSequence(sequence);
        header.setCompress(NONE.code);
        header.setVersion(Meta.version);

        if (message instanceof BatchMessage) {
            BatchMessage batchMessage = (BatchMessage) message;
            List<Message> messages = batchMessage.messages;

            byte[] extend = {1};
            header.setExtend(extend);
            header.setHeaderSize(FIXED_LENGTH + extend.length);
            int payloadLength = batchMessage.length + 4 * messages.size();
            header.setEntrySize(payloadLength + header.headerSize());

            ByteBuffer buffer = ByteBuffer.allocate(payloadLength);
            for (Message msg : messages) {
                buffer.putInt(msg.data.length);
                buffer.put(msg.data);
            }

            futureMap.put(sequence, message.future);
            return new SequentialEntry(header, buffer.array());

        }

        header.setHeaderSize(FIXED_LENGTH);
        header.setEntrySize(message.data.length + header.getHeaderSize());

        futureMap.put(sequence, message.future);
        return new SequentialEntry(header, message.data);
    }

    @Override
    public boolean isTriggerEvent(Event event) {
        TimeSizeEvent timeSizeEvent = (TimeSizeEvent) event;
        IndexFile indexFile = indexFileManager.get(appender.getSegment().baseSequence());
        return timeSizeEvent.getSize() > maxSegmentSize || indexFile.isFull();
    }

    @Override
    public void rollover() throws IOException {
        appender.flush();
        appender.close();
        appender.getSegment().setActive(false);
    }

    @Override
    public Segment nextSegment(SegmentDescriptor descriptor) throws IOException {
        Segment segment = segmentManager.create(descriptor.sequence());
        IndexFile indexFile = indexFileManager.create(descriptor.sequence());
        segment.setEntryListener(indexFile::receiveEntry);
        segment.setActive(true);
        return segment;
    }


    private class Message {
        private byte[] data;

        CompletableFuture<Boolean> future;

        Message() {
        }

        Message(byte[] data) {
            this.data = data;
        }

        Message(byte[] data, CompletableFuture<Boolean> future) {
            this.data = data;
            this.future = future;
        }
    }

    private class BatchMessage extends Message {
        private List<Message> messages;
        private int length;

        BatchMessage(List<Message> messages, CompletableFuture<Boolean> future) {
            this.messages = messages;
            this.future = future;
        }
    }
}

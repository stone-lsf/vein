package com.sm.finance.charge.storage.sequential.index;

import com.sm.finance.charge.common.utils.FileUtil;
import com.sm.finance.charge.common.utils.IoUtil;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.storage.api.exceptions.ClosedException;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.OffsetIndex;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:54
 */
public class SequentialIndexFile extends LogSupport implements IndexFile {
    private final File file;
    private final long baseSequence;
    private final int indexInterval;
    private final int maxFileSize;
    private final int maxEntries;
    private volatile MappedByteBuffer mapBuffer;

    private volatile boolean closed = false;


    private volatile int entries;
    private volatile long lastIndexSequence;


    SequentialIndexFile(File file, long baseSequence, int indexInterval, int maxFileSize) throws IOException {
        boolean newFile = file.createNewFile();
        this.file = file;
        this.baseSequence = baseSequence;
        this.indexInterval = indexInterval;
        this.maxFileSize = maxFileSize;
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        try {
            if (newFile) {
                raf.setLength(maxFileSize);
            }

            this.mapBuffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
            if (newFile) {
                mapBuffer.position(0);
            } else {
                mapBuffer.position(roundToExactMultiple(mapBuffer.limit(), SequentialOffsetIndex.LENGTH));
            }
            this.entries = mapBuffer.position() / SequentialOffsetIndex.LENGTH;
            this.maxEntries = mapBuffer.limit() / SequentialOffsetIndex.LENGTH;
        } finally {
            IoUtil.close(raf);
        }
    }

    private int roundToExactMultiple(int number, int factor) {
        return factor * (number / factor);
    }

    @Override
    public long baseSequence() {
        return baseSequence;
    }

    @Override
    public OffsetIndex lastIndex() {
        if (entries == 0) {
            return null;
        }

        long sequence = getSequence(mapBuffer, entries);
        long offset = getOffset(mapBuffer, entries);
        return new SequentialOffsetIndex(sequence, offset);
    }

    /**
     * 读取序列号
     *
     * @param mapBuffer buffer
     * @param num       索引文件的第num条记录
     * @return sequence
     */
    private long getSequence(ByteBuffer mapBuffer, int num) {
        return mapBuffer.getLong(num * SequentialOffsetIndex.LENGTH);
    }

    /**
     * 读取序列号
     *
     * @param mapBuffer buffer
     * @param num       索引文件的第num条记录
     * @return sequence
     */
    private long getOffset(ByteBuffer mapBuffer, int num) {
        return mapBuffer.getLong(num * SequentialOffsetIndex.LENGTH + 8);
    }

    @Override
    public OffsetIndex lookup(long sequence) {
        ByteBuffer duplicate = mapBuffer.duplicate();

        long interval = sequence - baseSequence;
        if (entries == 0) {
            return null;
        }

        int slot = (int) ((interval + indexInterval) / indexInterval);
        OffsetIndex offsetIndex = getOffsetIndex(duplicate, slot);
        while (offsetIndex.sequence() > sequence) {
            offsetIndex = getOffsetIndex(duplicate, slot--);
        }
        return offsetIndex;
    }

    private OffsetIndex getOffsetIndex(ByteBuffer buffer, int slot) {
        long sequence = getSequence(buffer, slot);
        long offset = getOffset(buffer, slot);
        return new SequentialOffsetIndex(sequence, offset);
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public IndexFile truncate(long offset) throws IOException {
        FileUtil.truncate(offset, file);
        resize();
        return this;
    }

    private void resize() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        int position = mapBuffer.position();
        try {
            this.mapBuffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
            mapBuffer.position(roundToExactMultiple(mapBuffer.limit(), SequentialOffsetIndex.LENGTH));

            this.entries = mapBuffer.position() / SequentialOffsetIndex.LENGTH;
            mapBuffer.position(position);
        } finally {
            IoUtil.close(raf);
        }
    }


    @Override
    public void receiveEntry(long sequence, long offset) {
        if (sequence - lastIndexSequence >= indexInterval) {
            SequentialOffsetIndex index = new SequentialOffsetIndex(sequence, offset);
            int checkSum = index.buildCheckSum();
            index.setCrc32(checkSum);
            index.writeTo(mapBuffer);
            entries += 1;
            lastIndexSequence = sequence;
        }
    }

    @Override
    public void trimToValidSize() throws IOException {
        truncate(entries * 8);
    }

    @Override
    public IndexFile flush() {
        checkClosed();
        mapBuffer.force();
        return this;
    }

    @Override
    public int maxFileSize() {
        return maxFileSize;
    }

    @Override
    public boolean isFull() {
        return entries >= maxEntries;
    }

    @Override
    public boolean delete() {
        //TODO delete
        return false;
    }

    @Override
    public void close() throws Exception {
        closed = true;
    }

    private void checkClosed() {
        if (closed) {
            throw new ClosedException(file.getName() + " appender has closed!");
        }
    }
}

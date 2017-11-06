package com.sm.charge.raft.server.storage.logs.index;

import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.common.utils.FileUtil;
import com.sm.finance.charge.common.utils.IoUtil;
import com.sm.finance.charge.storage.api.exceptions.ClosedException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static com.sm.charge.raft.server.storage.logs.index.LogIndex.LENGTH;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 上午11:54
 */
public class OffsetIndex extends LoggerSupport {
    private static final int OFFSET_SIZE = 8;
    private static final int POSITION_SIZE = 4;
    private static final int CHECKSUM_SIZE = 8;
    private static final int ENTRY_SIZE = OFFSET_SIZE + POSITION_SIZE + CHECKSUM_SIZE;

    private final File file;
    private final long baseIndex;
    private final int maxFileSize;
    private volatile MappedByteBuffer mapBuffer;

    private volatile boolean closed = false;
    private volatile int entries;
    private volatile long lastIndex;

    public OffsetIndex(File file, long baseIndex, int maxEntries) {
        RandomAccessFile raf = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            this.file = file;
            this.baseIndex = baseIndex;

            this.maxFileSize = maxEntries * ENTRY_SIZE;
            raf = new RandomAccessFile(file, "rw");
            raf.setLength(maxFileSize);

            this.mapBuffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
            this.mapBuffer.position(0);
            this.entries = 0;
            lastIndex = baseIndex - 1;
        } catch (IOException e) {
            logger.error("new OffsetIndex:{} caught exception", file, e);
            throw new StorageException(e);
        } finally {
            IoUtil.close(raf);
        }
    }

    public OffsetIndex(File file, long baseIndex) {
        RandomAccessFile raf = null;
        try {
            this.file = file;
            this.baseIndex = baseIndex;
            raf = new RandomAccessFile(file, "r");

            this.mapBuffer = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
            this.mapBuffer.position(roundToExactMultiple(mapBuffer.limit(), ENTRY_SIZE));
            this.maxFileSize = mapBuffer.limit();
            this.entries = mapBuffer.position() / ENTRY_SIZE;

            LogIndex logIndex = lastIndex();
            if (logIndex == null) {
                lastIndex = baseIndex - 1;
            } else {
                lastIndex = logIndex.getIndex();
            }
        } catch (IOException e) {
            logger.error("new OffsetIndex:{} caught exception", file, e);
            throw new StorageException(e);
        } finally {
            IoUtil.close(raf);
        }
    }


    private int roundToExactMultiple(int number, int factor) {
        return factor * (number / factor);
    }

    public LogIndex firstIndex() {
        if (entries == 0) {
            return null;
        }

        long index = getIndex(mapBuffer, 0);
        long offset = getOffset(mapBuffer, 0);
        return new LogIndex(index, (int) offset);
    }

    public LogIndex lastIndex() {
        if (entries == 0) {
            return null;
        }

        long index = getIndex(mapBuffer, entries - 1);
        long offset = getOffset(mapBuffer, entries - 1);
        return new LogIndex(index, (int) offset);
    }

    /**
     * 读取index
     *
     * @param mapBuffer buffer
     * @param num       索引文件的第num条记录
     * @return index
     */
    private long getIndex(ByteBuffer mapBuffer, int num) {
        return mapBuffer.getLong(num * ENTRY_SIZE);
    }

    /**
     * 读取index
     *
     * @param mapBuffer buffer
     * @param num       索引文件的第num条记录
     * @return index
     */
    private long getOffset(ByteBuffer mapBuffer, int num) {
        return mapBuffer.getInt(num * ENTRY_SIZE + 8);
    }


    public LogIndex lookup(long index) {
        ByteBuffer duplicate = mapBuffer.duplicate();

        long interval = index - baseIndex;
        if (entries == 0) {
            return null;
        }

        int slot = (int) interval;
        LogIndex offsetIndex = getOffsetIndex(duplicate, slot);
        while (offsetIndex.getIndex() > index) {
            offsetIndex = getOffsetIndex(duplicate, slot--);
        }
        return offsetIndex;
    }

    private LogIndex getOffsetIndex(ByteBuffer buffer, int slot) {
        long index = getIndex(buffer, slot);
        long offset = getOffset(buffer, slot);
        return new LogIndex(index, (int) offset);
    }


    public File getFile() {
        return file;
    }

    public void indexEntry(long index, long offset) {
        ByteBuffer buffer = ByteBuffer.allocate(LENGTH);
        buffer.putLong(index);
        buffer.putInt((int) offset);

        Checksum crc32 = new CRC32();
        crc32.update(buffer.array(), 0, LENGTH);
        long checksum = crc32.getValue();

        mapBuffer.putLong(index);
        mapBuffer.putInt((int) offset);
        mapBuffer.putLong(checksum);

        entries += 1;
        lastIndex = index;
    }


    public void trimToValidSize() throws IOException {
        FileUtil.truncate(entries * ENTRY_SIZE, file);
    }


    public void flush() {
        checkClosed();
        mapBuffer.force();
    }

    public long getLastIndex() {
        return lastIndex;
    }

    public int size() {
        return entries;
    }

    public boolean delete() {
        if (!closed) {
            logger.error("try to delete a unclosed index file:{}", file.getName());
            throw new IllegalStateException();
        }
        return file.delete();
    }


    public void close() {
        try {
            FileUtil.close(mapBuffer);
        } catch (ReflectiveOperationException e) {
            logger.error("close mapped byte buffer caught exception ", e);
            throw new StorageException(e);
        }
        closed = true;
    }

    private void checkClosed() {
        if (closed) {
            throw new ClosedException(file.getName() + " appender has closed!");
        }
    }
}

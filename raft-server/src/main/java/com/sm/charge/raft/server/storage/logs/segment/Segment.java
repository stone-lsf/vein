package com.sm.charge.raft.server.storage.logs.segment;

import com.sm.charge.raft.client.Command;
import com.sm.charge.raft.server.storage.logs.entry.LogEntry;
import com.sm.charge.raft.server.storage.logs.index.LogIndex;
import com.sm.charge.raft.server.storage.logs.index.OffsetIndex;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.serializer.api.Serializer;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static com.sm.charge.raft.server.storage.logs.entry.LogEntry.INDEX_TERM_LENGTH;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 上午11:50
 */
public class Segment extends LoggerSupport {

    private final File file;
    private final long baseIndex;
    private final Serializer serializer;
    private OffsetIndex offsetIndex;
    //TODO 调整buffer 大小
    private final ByteBuffer buffer = ByteBuffer.allocate(1000000);
    private final FileChannel fileChannel;
    private volatile long writePosition;

    public Segment(File file, long baseIndex, Serializer serializer) {
        this.file = file;
        this.baseIndex = baseIndex;
        this.serializer = serializer;

        try {
            RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
            this.fileChannel = accessFile.getChannel();
        } catch (Exception e) {
            logger.error("get file:{} channel caught exception", file);
            throw new IllegalStateException(e);
        }
    }

    void buildIndex(int maxSegmentEntries) {
        File indexFile = buildIndexFile();
        this.offsetIndex = new OffsetIndex(indexFile, baseIndex, maxSegmentEntries);
    }

    private File buildIndexFile() {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf(SegmentManager.EXTENSION));
        fileName += "index";
        return new File(file.getParent(), fileName);
    }


    public long append(LogEntry entry) {
        long index = nextIndex();
        entry.setIndex(index);
        byte[] bytes = serializer.serialize(entry.getCommand());
        buffer.clear();
        int totalLength = INDEX_TERM_LENGTH + bytes.length;
        entry.setSize(totalLength);

        buffer.putInt(totalLength);
        buffer.putLong(entry.getIndex());
        buffer.putLong(entry.getTerm());
        buffer.put(bytes);

        byte[] array = buffer.array();
        Checksum crc32 = new CRC32();
        crc32.update(array, 4, totalLength);
        long checksum = crc32.getValue();
        buffer.putLong(checksum);

        buffer.flip();
        long position = writePosition;
        try {
            fileChannel.position(writePosition);
            while (buffer.hasRemaining()) {
                fileChannel.write(buffer);
            }
            writePosition = fileChannel.position();
        } catch (IOException e) {
            throw new StorageException(e);
        }

        offsetIndex.indexEntry(index, position);

        return index;
    }

    public LogEntry get(long index) {
        LogIndex logIndex = offsetIndex.lookup(index);
        if (logIndex == null) {
            return null;
        }

        if (logIndex.getIndex() != index) {
            return null;
        }

        return readEntry(logIndex.getOffset());
    }

    private LogEntry readEntry(long position) {
        ByteBuffer length;
        ByteBuffer body;

        int size;
        try {
            fileChannel.position(position);
            length = ByteBuffer.allocate(4);
            while (length.hasRemaining()) {
                fileChannel.read(length);
            }

            length.flip();
            size = length.getInt();
            body = ByteBuffer.allocate(size + 8);
            while (body.hasRemaining()) {
                fileChannel.read(body);
            }
        } catch (IOException e) {
            logger.error("locate segment:{} position:{} caught exception", file, position, e);
            throw new StorageException(e);
        }

        body.flip();
        long index = body.getLong();
        long term = body.getLong();
        byte[] bytes = new byte[size - INDEX_TERM_LENGTH];
        body.get(bytes);
        long checksum = body.getLong();


        Checksum crc32 = new CRC32();
        crc32.update(body.array(), 0, size);
        if (crc32.getValue() != checksum) {
            throw new BadDataException();
        }

        Command command = serializer.deserialize(bytes);
        return new LogEntry(index, term, size, command);
    }

    public void rebuildIndex(int maxSegmentEntries) {
        File file = buildIndexFile();
        if (file.exists()) {
            file.delete();
        }
        offsetIndex = new OffsetIndex(file, baseIndex, maxSegmentEntries);
        long size;
        try {
            offsetIndex.truncate(0);
            size = fileChannel.size();
        } catch (IOException e) {
            logger.error("truncate segment:{} position:{} caught exception", this.file, 0, e);
            throw new StorageException(e);
        }

        long position = 0;
        while (position < size) {
            try {
                LogEntry entry = readEntry(position);
                offsetIndex.indexEntry(entry.getIndex(), position);
                position = fileChannel.position();
            } catch (BadDataException e) {
                logger.error("segment:{} from position:{} data has bad!", this.file, position, e);
                break;
            } catch (IOException e) {
                logger.error("read entry from segment:{} position:{} caught exception", this.file, position, e);
                throw new StorageException(e);
            }
        }
        writePosition = position;
    }

    public long nextIndex() {
        return offsetIndex.getLastIndex() + 1;
    }

    public int size() {
        try {
            return (int) fileChannel.size();
        } catch (IOException e) {
            logger.error("get segment:{} size caught exception", file, e);
            throw new StorageException(e);
        }
    }

    public int entries() {
        return offsetIndex.size();
    }

    public void flush() {
        try {
            fileChannel.force(false);
        } catch (IOException e) {
            logger.error("flush segment:{}  caught exception", file, e);
            throw new StorageException(e);
        }
    }

    public void close() {
        try {
            fileChannel.close();
        } catch (IOException e) {
            logger.error("flush segment:{}  caught exception", file, e);
            throw new StorageException(e);
        }
    }
}

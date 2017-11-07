package com.sm.charge.raft.server.storage.snapshot.file;

import com.sm.charge.buffer.BooleanValue;
import com.sm.charge.buffer.BufferStream;
import com.sm.charge.buffer.BytesBuffer;
import com.sm.charge.raft.server.storage.snapshot.Entry;
import com.sm.charge.raft.server.storage.snapshot.SnapshotWriter;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.common.utils.FileUtil;
import com.sm.finance.charge.common.utils.IoUtil;
import com.sm.finance.charge.serializer.api.Serializable;
import com.sm.finance.charge.serializer.api.Serializer;
import com.sm.finance.charge.storage.api.exceptions.StorageException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午10:51
 */
public class FileSnapshotWriter extends LoggerSupport implements SnapshotWriter {
    private final MappedByteBuffer mapBuffer;
    private final File file;
    private final Serializer serializer;

    FileSnapshotWriter(File file, Serializer serializer) {
        this.serializer = serializer;
        this.file = file;
        RandomAccessFile raf = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            raf = new RandomAccessFile(file, "rw");
            raf.setLength(Integer.MAX_VALUE);

            this.mapBuffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
            this.mapBuffer.position(8);
        } catch (IOException e) {
            logger.error("new OffsetIndex:{} caught exception", file, e);
            throw new StorageException(e);
        } finally {
            IoUtil.close(raf);
        }
    }


    @Override
    public SnapshotWriter write(BytesBuffer bytes) {
        return this;
    }

    @Override
    public SnapshotWriter write(byte[] bytes) {
        mapBuffer.put(bytes);
        return this;
    }

    @Override
    public SnapshotWriter write(BytesBuffer bytes, int offset, int length) {
        return this;
    }

    @Override
    public SnapshotWriter write(byte[] bytes, int offset, int length) {
        mapBuffer.put(bytes, offset, length);
        return this;
    }

    @Override
    public SnapshotWriter write(BufferStream buffer) {
        return this;
    }

    @Override
    public SnapshotWriter writeBoolean(boolean v) {
        mapBuffer.put(v ? BooleanValue.TRUE : BooleanValue.FALSE);
        return this;
    }

    @Override
    public SnapshotWriter writeByte(byte b) {
        mapBuffer.put(b);
        return this;
    }


    @Override
    public SnapshotWriter writeChar(char c) {
        mapBuffer.putChar(c);
        return this;
    }

    @Override
    public SnapshotWriter writeShort(short s) {
        mapBuffer.putShort(s);
        return this;
    }


    @Override
    public SnapshotWriter writeInt(int i) {
        mapBuffer.putInt(i);
        return this;
    }

    @Override
    public SnapshotWriter writeLong(long l) {
        mapBuffer.putLong(l);
        return this;
    }

    @Override
    public SnapshotWriter writeFloat(float f) {
        mapBuffer.putFloat(f);
        return this;
    }

    @Override
    public SnapshotWriter writeDouble(double d) {
        mapBuffer.putDouble(d);
        return this;
    }

    @Override
    public SnapshotWriter writeString(String s) {
        byte[] bytes = s.getBytes();
        mapBuffer.putInt(bytes.length);
        mapBuffer.put(bytes);
        return this;
    }

    @Override
    public SnapshotWriter writeUTF8(String s) {
        try {
            byte[] bytes = s.getBytes("UTF-8");
            mapBuffer.putInt(bytes.length);
            mapBuffer.put(bytes);
        } catch (UnsupportedEncodingException e) {
            logger.error("unsupported encode:{}", "UTF-8");
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public SnapshotWriter flush() {
        mapBuffer.force();
        return this;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public SnapshotWriter skip(int size) {
        mapBuffer.position(mapBuffer.position() + size);
        return this;
    }

    @Override
    public long position() {
        return mapBuffer.position();
    }

    @Override
    public SnapshotWriter writeObject(Serializable object) {
        byte[] payload = serializer.serialize(object);
        Entry entry = new Entry(payload);
        entry.writeTo(mapBuffer);
        return this;
    }

    @Override
    public SnapshotWriter trimToValidSize() {
        try {
            FileUtil.truncate(mapBuffer.position(), file);
            return this;
        } catch (IOException e) {
            logger.error("truncate file:{} caught exception", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            FileUtil.close(mapBuffer);
        } catch (ReflectiveOperationException e) {
            logger.error("close snapshot:{} writer fail", file, e);
            throw new RuntimeException(e);
        }
    }
}

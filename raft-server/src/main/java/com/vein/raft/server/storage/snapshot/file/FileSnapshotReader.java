package com.vein.raft.server.storage.snapshot.file;


import com.vein.buffer.BooleanValue;
import com.vein.buffer.BufferStream;
import com.vein.buffer.BytesBuffer;
import com.vein.raft.server.storage.snapshot.SnapshotReader;
import com.vein.common.base.LoggerSupport;
import com.vein.common.utils.FileUtil;
import com.vein.common.utils.IoUtil;
import com.vein.serializer.api.Serializer;
import com.vein.storage.api.exceptions.StorageException;

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
public class FileSnapshotReader extends LoggerSupport implements SnapshotReader {
    private final MappedByteBuffer mapBuffer;
    private final Serializer serializer;
    private final File file;
    private final long size;

    public FileSnapshotReader(Serializer serializer, File file) {
        this.serializer = serializer;
        this.file = file;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            this.mapBuffer = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
            this.mapBuffer.position(8);
            this.size = mapBuffer.limit();
        } catch (IOException e) {
            logger.error("new OffsetIndex:{} caught exception", file, e);
            throw new StorageException(e);
        } finally {
            IoUtil.close(raf);
        }
    }

    @Override
    public long remaining() {
        return size - mapBuffer.position();
    }

    @Override
    public SnapshotReader read(byte[] bytes) {
        mapBuffer.get(bytes);
        return null;
    }

    @Override
    public SnapshotReader read(BytesBuffer bytes, long offset, long length) {
        return null;
    }

    @Override
    public SnapshotReader read(byte[] bytes, int offset, int length) {
        mapBuffer.get(bytes, offset, length);
        return null;
    }

    @Override
    public SnapshotReader read(BufferStream buffer) {
        return null;
    }

    @Override
    public byte readByte() {
        return mapBuffer.get();
    }


    @Override
    public short readShort() {
        return mapBuffer.getShort();
    }


    @Override
    public char readChar() {
        return mapBuffer.getChar();
    }

    @Override
    public int readInt() {
        return mapBuffer.getInt();
    }


    @Override
    public long readLong() {
        return mapBuffer.getLong();
    }

    @Override
    public float readFloat() {
        return mapBuffer.getFloat();
    }

    @Override
    public double readDouble() {
        return mapBuffer.getDouble();
    }

    @Override
    public boolean readBoolean() {
        byte b = mapBuffer.get();
        return b == BooleanValue.TRUE;
    }

    @Override
    public String readString() {
        int size = mapBuffer.getInt();
        byte[] bytes = new byte[size];
        mapBuffer.get(bytes);
        return new String(bytes);
    }

    @Override
    public String readUTF8() {
        int size = mapBuffer.getInt();
        byte[] bytes = new byte[size];
        mapBuffer.get(bytes);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("unsupported encode:{}", "UTF-8");
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasRemaining() {
        return size > mapBuffer.position();
    }

    @Override
    public SnapshotReader skip(int bytes) {
        int position = mapBuffer.position();
        mapBuffer.position(position + bytes);
        return this;
    }

    @Override
    public SnapshotReader read(BytesBuffer bytes) {
        return null;
    }

    @Override
    public void close() {
        try {
            FileUtil.close(mapBuffer);
        } catch (ReflectiveOperationException e) {
            logger.error("close snapshot:{} reader fail", file, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public File getFile() {
        return file;
    }
}

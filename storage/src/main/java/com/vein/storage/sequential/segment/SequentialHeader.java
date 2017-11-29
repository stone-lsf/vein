package com.vein.storage.sequential.segment;

import com.vein.storage.api.segment.Header;

import java.nio.ByteBuffer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 上午12:05
 */
public class SequentialHeader implements Header {
    public static final int FIXED_LENGTH = 8 + 4 + 4 + 4 + 2;
    private ByteBuffer length = ByteBuffer.allocate(4);
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private boolean readComplete;
    private boolean writeComplete;

    private int headerSize;
    private int entrySize;
    private long sequence;
    private int crc32;
    private byte version;
    private byte compress;
    private byte[] extend;

    @Override
    public long sequence() {
        return sequence;
    }

    @Override
    public int entrySize() {
        return entrySize;
    }

    @Override
    public int headerSize() {
        return headerSize;
    }

    @Override
    public int crc32() {
        return crc32;
    }

    @Override
    public byte version() {
        return version;
    }

    @Override
    public byte compress() {
        return compress;
    }

    @Override
    public byte[] extend() {
        return extend;
    }

    @Override
    public int size() {
        return headerSize;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public int getEntrySize() {
        return entrySize;
    }

    public void setEntrySize(int entrySize) {
        this.entrySize = entrySize;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public int getCrc32() {
        return crc32;
    }

    public void setCrc32(int crc32) {
        this.crc32 = crc32;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getCompress() {
        return compress;
    }

    public void setCompress(byte compress) {
        this.compress = compress;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    @Override
    public boolean readComplete() {
        return readComplete;
    }

    @Override
    public boolean writeComplete() {
        return writeComplete;
    }

    @Override
    public void writeTo(ByteBuffer buffer) {
        if (writeComplete()) {
            return;
        }

        if (writeBuffer == null) {
            writeBuffer = ByteBuffer.allocate(headerSize);
            writeBuffer.putInt(headerSize);
            writeBuffer.putInt(entrySize);
            writeBuffer.putLong(sequence);
            writeBuffer.putInt(crc32);
            writeBuffer.put(version);
            writeBuffer.put(compress);
            writeBuffer.put(extend);
            writeBuffer.flip();
        }

        buffer.put(writeBuffer);
        if (writeBuffer.hasRemaining()) {
            return;
        }

        writeComplete = true;
        writeBuffer = null;
    }

    @Override
    public void readFrom(ByteBuffer buffer) {
        if (readComplete()) {
            return;
        }

        if (length.hasRemaining()) {
            length.put(buffer);
            if (readBuffer.hasRemaining()) {
                return;
            }
        }

        if (readBuffer == null) {
            length.flip();
            this.headerSize = length.getInt();
            readBuffer = ByteBuffer.allocate(headerSize - 4);
            length.flip();
        }

        readBuffer.put(buffer);
        if (readBuffer.hasRemaining()) {
            return;
        }

        readBuffer.flip();
        this.entrySize = readBuffer.getInt();
        this.sequence = readBuffer.getLong();
        this.crc32 = readBuffer.getInt();
        this.version = readBuffer.get();
        this.compress = readBuffer.get();
        this.extend = new byte[this.headerSize - FIXED_LENGTH];
        readBuffer.get(extend);

        this.readComplete = true;
        readBuffer = null;
    }
}

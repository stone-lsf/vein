package com.sm.finance.charge.storage.sequential.index;

import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.index.OffsetIndex;

import java.nio.ByteBuffer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:54
 */
public class SequentialOffsetIndex implements OffsetIndex {
    public static final int LENGTH = 8 + 8 + 4;

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private boolean readComplete;
    private boolean writeComplete;

    private long sequence;
    private long offset;
    private int crc32;

    public SequentialOffsetIndex() {
    }

    public SequentialOffsetIndex(long sequence, long offset) {
        this.sequence = sequence;
        this.offset = offset;
    }

    @Override
    public long sequence() {
        return sequence;
    }

    @Override
    public long offset() {
        return offset;
    }

    @Override
    public int crc32() {
        return crc32;
    }

    @Override
    public int size() {
        return LENGTH;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setCrc32(int crc32) {
        this.crc32 = crc32;
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
            writeBuffer = ByteBuffer.allocate(LENGTH);
            writeBuffer.putLong(sequence);
            writeBuffer.putLong(offset);
            writeBuffer.putInt(crc32);
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

        if (readBuffer == null) {
            readBuffer = ByteBuffer.allocate(LENGTH);
        }

        readBuffer.put(buffer);
        if (readBuffer.hasRemaining()) {
            return;
        }

        readBuffer.flip();
        this.sequence = readBuffer.getLong();
        this.offset = readBuffer.getLong();
        this.crc32 = readBuffer.getInt();

        this.readComplete = true;
        readBuffer = null;
    }

    @Override
    public int buildCheckSum() {
        return 0;
    }

    @Override
    public void validCheckSum() throws BadDataException {

    }
}

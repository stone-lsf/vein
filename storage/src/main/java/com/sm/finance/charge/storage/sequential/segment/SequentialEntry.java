package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Header;

import java.nio.ByteBuffer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 上午12:05
 */
public class SequentialEntry implements Entry {

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private boolean readComplete;
    private boolean writeComplete;

    private Header header;
    private byte[] payload;

    @Override
    public Header head() {
        return header;
    }

    @Override
    public byte[] payload() {
        return payload;
    }

    @Override
    public int size() {
        return header.size() + payload.length;
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
            writeBuffer = ByteBuffer.allocate(payload.length);
            writeBuffer.put(payload);
            writeBuffer.flip();
        }

        if (!header.writeComplete()) {
            header.writeTo(buffer);
            if (!header.writeComplete()) {
                return;
            }
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

        if (header == null) {
            header = new SequentialHeader();
        }

        if (!header.readComplete()) {
            header.readFrom(buffer);
            if (!header.readComplete()) {
                return;
            }
        }

        if (readBuffer == null) {
            int entrySize = header.entrySize();
            int headerSize = header.headerSize();
            int payloadLength = entrySize - headerSize;
            readBuffer = ByteBuffer.allocate(payloadLength);
            payload = new byte[payloadLength];
        }

        readBuffer.put(buffer);

        if (readBuffer.hasRemaining()) {
            return;
        }

        readBuffer.flip();
        readBuffer.get(payload);

        this.readComplete = true;
        this.readBuffer = null;
    }

    @Override
    public int calculate() {
        return 0;
    }

    @Override
    public void check() throws BadDataException {

    }
}

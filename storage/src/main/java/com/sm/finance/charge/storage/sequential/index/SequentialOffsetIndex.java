package com.sm.finance.charge.storage.sequential.index;

import com.sm.finance.charge.storage.api.index.OffsetIndex;

import java.nio.ByteBuffer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:54
 */
public class SequentialOffsetIndex implements OffsetIndex {
    public static final int LENGTH = 8 + 8 + 4;

    private long sequence;
    private long offset;
    private int crc32;

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

    @Override
    public boolean initComplete() {
        return false;
    }

    @Override
    public void writeTo(ByteBuffer buffer) {

    }

    @Override
    public void readFrom(ByteBuffer buffer) {

    }
}

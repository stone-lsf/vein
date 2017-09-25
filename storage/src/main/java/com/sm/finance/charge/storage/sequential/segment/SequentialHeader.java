package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.storage.api.segment.Header;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 上午12:05
 */
public class SequentialHeader implements Header {
    private long sequence;
    private int entrySize;
    private int headerSize;
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
}

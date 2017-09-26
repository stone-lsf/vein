package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Header;

import java.nio.ByteBuffer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 上午12:05
 */
public class SequentialEntry implements Entry {

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

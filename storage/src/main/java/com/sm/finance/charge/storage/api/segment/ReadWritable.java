package com.sm.finance.charge.storage.api.segment;

import java.nio.ByteBuffer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/27 上午12:17
 */
public interface ReadWritable {

    int size();

    boolean initComplete();

    void writeTo(ByteBuffer buffer);

    void readFrom(ByteBuffer buffer);
}

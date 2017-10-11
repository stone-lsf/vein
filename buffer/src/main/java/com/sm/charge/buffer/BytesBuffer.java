package com.sm.charge.buffer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/7 下午9:17
 */
public interface BytesBuffer extends BytesReader<BytesBuffer>, BytesWriter<BytesBuffer> {
    int BYTE = 1;
    int BOOLEAN = 1;
    int CHARACTER = 2;
    int SHORT = 2;
    int MEDIUM = 3;
    int INTEGER = 4;
    int LONG = 8;
    int FLOAT = 4;
    int DOUBLE = 8;

    boolean hasArray();

    byte[] array();

    long size();

    long offset();

    long capacity();

    long position();

    long limit();

    BytesBuffer flip();

    BytesBuffer mark();

    BytesBuffer reset();

    BytesBuffer rewind();

    BytesBuffer clear();

    BytesBuffer compact();
}

package com.vein.buffer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/7 下午9:12
 */
public interface BufferInputStream<T extends BufferInputStream<?>> extends AutoCloseable {

    long remaining();

    boolean hasRemaining();

    T skip(int bytes);

    T read(BytesBuffer bytes);

    T read(byte[] bytes);

    T read(BytesBuffer bytes, long offset, long length);

    T read(byte[] bytes, int offset, int length);

    T read(BufferStream buffer);

    byte readByte();

    short readShort();

    char readChar();

    int readInt();

    long readLong();

    float readFloat();

    double readDouble();

    boolean readBoolean();

    String readString();

    String readUTF8();
}

package com.sm.charge.buffer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/7 下午9:12
 */
public interface BufferInputStream<T extends BufferInputStream<?>> extends AutoCloseable {

    long remaining();

    boolean hasRemaining();

    T skip(long bytes);

    T read(BytesBuffer bytes);

    T read(byte[] bytes);

    T read(BytesBuffer bytes, long offset, long length);

    T read(byte[] bytes, long offset, long length);

    T read(BufferStream buffer);

    byte readByte();

    int readUnsignedByte();

    short readShort();

    int readUnsignedShort();

    char readChar();

    int readInt();

    long readUnsignedInt();

    long readLong();

    float readFloat();

    double readDouble();

    boolean readBoolean();

    String readString();

    String readUTF8();
}

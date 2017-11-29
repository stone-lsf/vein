package com.vein.buffer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/7 下午10:11
 */
public interface BytesReader<T extends BytesReader<T>> {

    long remaining();

    boolean hasRemaining();

    T skip(long bytes);

    T get(BytesBuffer bytes);

    T get(byte[] bytes);

    T get(BytesBuffer bytes, long offset, long length);

    T get(byte[] bytes, long offset, long length);

    T get(BufferStream buffer);

    byte getByte();

    int getUnsignedByte();

    short getShort();

    int getUnsignedShort();

    char getChar();

    int getInt();

    long getUnsignedInt();

    long getLong();

    float getFloat();

    double getDouble();

    boolean getBoolean();

    String getString();

    String getUTF8();

}

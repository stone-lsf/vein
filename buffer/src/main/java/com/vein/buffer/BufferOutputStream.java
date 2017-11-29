package com.vein.buffer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/7 下午9:12
 */
public interface BufferOutputStream<T extends BufferOutputStream<?>> extends AutoCloseable {

    T write(BytesBuffer bytes);

    T write(byte[] bytes);

    T write(BytesBuffer bytes, int offset, int length);

    T write(byte[] bytes, int offset, int length);

    T write(BufferStream buffer);

    T writeBoolean(boolean v);

    T writeByte(byte b);

    T writeChar(char c);

    T writeShort(short s);

    T writeInt(int i);

    T writeLong(long l);

    T writeFloat(float f);

    T writeDouble(double d);

    T writeString(String s);

    T writeUTF8(String s);

    T flush();
}

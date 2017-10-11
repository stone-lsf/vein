package com.sm.charge.buffer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/7 下午9:12
 */
public interface BufferOutputStream<T extends BufferOutputStream<?>> extends AutoCloseable {

    T write(BytesBuffer bytes);

    T write(byte[] bytes);

    T write(BytesBuffer bytes, long offset, long length);

    T write(byte[] bytes, long offset, long length);

    T write(BufferStream buffer);

    T writeBoolean(boolean v);

    T writeByte(int b);

    T writeUnsignedByte(int b);

    T writeChar(char c);

    T writeShort(short s);

    T writeUnsignedShort(int s);

    T writeInt(int i);

    T writeUnsignedInt(long i);

    T writeLong(long l);

    T writeFloat(float f);

    T writeDouble(double d);

    T writeString(String s);

    T writeUTF8(String s);

    T flush();
}

package com.vein.buffer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/7 下午10:11
 */
public interface BytesWriter<T extends BytesReader<T>> {
    T put(BytesBuffer bytes);

    T put(byte[] bytes);

    T put(BytesBuffer bytes, long offset, long length);

    T put(byte[] bytes, long offset, long length);

    T put(BufferStream buffer);

    T putBoolean(boolean v);

    T putByte(int b);

    T putUnsignedByte(int b);

    T putChar(char c);

    T putShort(short s);

    T putUnsignedShort(int s);

    T putInt(int i);

    T putUnsignedInt(long i);

    T putLong(long l);

    T putFloat(float f);

    T putDouble(double d);

    T putString(String s);

    T putUTF8(String s);
}

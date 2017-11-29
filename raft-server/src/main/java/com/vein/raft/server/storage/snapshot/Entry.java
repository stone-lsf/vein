package com.vein.raft.server.storage.snapshot;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * @author shifeng.luo
 * @version created on 2017/11/6 下午2:11
 */
public class Entry {

    private int size;
    private byte[] payload;
    private long checksum;

    public Entry() {
    }

    public Entry(byte[] payload) {
        this.payload = payload;
        this.size = payload.length;
    }


    public void writeTo(ByteBuffer buffer) {
        this.checksum = checksum();

        buffer.putInt(size);
        buffer.put(payload);
        buffer.putLong(checksum);
    }

    public long checksum() {
        ByteBuffer bf = ByteBuffer.allocate(4 + payload.length);
        bf.putInt(size);
        bf.put(payload);

        CRC32 crc32 = new CRC32();
        crc32.update(bf);
        return crc32.getValue();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }
}

package com.sm.charge.raft.server.storage.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午10:51
 */
public class FileSnapshotWriter {

    private FileChannel fileChannel;
    private FileOutputStream fileOutputStream;
    private OutputStream outputStream;


    public static void main(String[] args) throws IOException {
        String path = "/Users/shifengluo";

        File file = new File(path, "minBuffer.txt");
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        byte[] data = new byte[1024 * 1024];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1024; i++) {
            channel.write(buffer);
            buffer.rewind();
        }
        channel.force(false);
        System.out.println("use:" + (System.currentTimeMillis() - start));
    }
}

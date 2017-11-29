package com.vein.raft.server.storage.snapshot.file;

import com.vein.raft.server.storage.snapshot.SnapshotReader;
import com.vein.raft.server.storage.snapshot.SnapshotWriter;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/11/6 下午8:19
 */
public class FileSnapshotWriterTest {

    private SnapshotWriter writer;
    private SnapshotReader reader;

    @Before
    public void init() {
        String name = "/Users/shifengluo/logs/test.txt";
        writer = new FileSnapshotWriter(new File(name), null);
        reader = new FileSnapshotReader(null, new File(name));
    }

    @Test
    public void writeString() throws Exception {
        String test = "asdkfjlasdjflaskjdflsakjfdlk";
        writer.writeString(test);
        writer.flush();
        writer.close();
        String string = reader.readString();
        System.out.println(string);
    }

    @Test
    public void trimToValidSize() throws Exception {
        String test = "asdkfjlasdjflaskjdflsakjfdlk";
        writer.writeString(test);
        writer.trimToValidSize();
        writer.flush();
        writer.close();
        String string = reader.readString();
        System.out.println(string);
    }

}
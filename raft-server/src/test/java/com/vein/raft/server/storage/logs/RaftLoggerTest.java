package com.vein.raft.server.storage.logs;

import com.vein.raft.server.storage.logs.entry.LogEntry;
import com.vein.common.utils.JsonUtil;
import com.vein.serializer.api.Serializer;
import com.vein.serializer.json.JsonSerializer;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午3:38
 */
public class RaftLoggerTest {

    private RaftLogger raftLogger;
    private File directory;
    private int maxSegmentSize;
    private int maxSegmentEntries;
    private int maxMessageSize;
    private Serializer serializer;

    @Before
    public void setUp() throws Exception {
        directory = new File("/Users/shifengluo/logs/raft");
        maxSegmentSize = 20 * 1024 * 1024;
        maxSegmentEntries = 10000;
        maxMessageSize = 10000;
        serializer = new JsonSerializer(new RaftSerializableTypes());

        raftLogger = new RaftLogger("raftLog", directory, serializer, maxSegmentSize, maxMessageSize, maxSegmentEntries);
    }

    @Test
    public void append() throws Exception {
        LogEntry entry = new LogEntry(new TestCommand("test"), 1);
        int i = 200;
        try {
            for (; i < 13000; i++) {
                entry.setIndex(i);
                raftLogger.append(entry);
            }
        } catch (Exception e) {
            System.out.println(i);
            e.printStackTrace();
        }
    }

    @Test
    public void get() throws Exception {

        LogEntry entry = raftLogger.get(100);
        System.out.println(entry.getIndex());
        System.out.println(JsonUtil.toJson(entry));
    }

}
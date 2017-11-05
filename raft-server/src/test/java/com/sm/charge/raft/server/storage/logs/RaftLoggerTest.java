package com.sm.charge.raft.server.storage.logs;

import com.sm.charge.raft.server.storage.logs.entry.LogEntry;
import com.sm.finance.charge.serializer.api.Serializer;
import com.sm.finance.charge.serializer.json.JsonSerializer;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午3:38
 */
public class RaftLoggerTest {

    private RaftLogger raftLogger;
    private File direcory;
    private int maxSegmentSize;
    private int maxOffsetIndexSize;
    private int maxSegmentEntries;
    private Serializer serializer;

    @Before
    public void setUp() throws Exception {
        direcory = new File("/Users/shifengluo/logs/raft");
        maxSegmentSize = 20 * 1024 * 1024;
        maxOffsetIndexSize = 2 * 1024 * 1024;
        maxSegmentEntries = 10000;
        serializer = new JsonSerializer(new RaftDataStructure());

        raftLogger = new RaftLogger("raftLog", direcory, serializer, maxSegmentSize, maxOffsetIndexSize, maxSegmentEntries);
    }

    @Test
    public void append() throws Exception {
        LogEntry entry = new LogEntry(new TestCommand("test"), 1);

        for (int i = 0; i < 100; i++) {
            entry.setIndex(i);
            raftLogger.append(entry);
        }
    }

    @Test
    public void get() throws Exception {
    }

}
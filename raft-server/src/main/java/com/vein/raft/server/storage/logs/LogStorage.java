package com.vein.raft.server.storage.logs;

import com.vein.serializer.api.Serializer;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午12:18
 */
public class LogStorage {
    private final File directory;
    private final int maxSegmentSize;
    private final int maxMessageSize;
    private final Serializer serializer;

    public LogStorage(File directory, int maxSegmentSize, int maxMessageSize, Serializer serializer) {
        this.directory = directory;
        this.maxSegmentSize = maxSegmentSize;
        this.maxMessageSize = maxMessageSize;
        this.serializer = serializer;
    }

    /**
     * 创建一个RaftLogger
     *
     * @param name 日志文件的名称
     * @return {@link RaftLogger}
     */
    public RaftLogger createLogger(String name) {
//        return new RaftLogger(name, directory, serializer, directory1, maxSegmentSize, maxOffsetIndexSize, maxMessageSize);
        return null;
    }

    /**
     * 返回存储目录
     *
     * @return 目录
     */
    public File directory() {
        return directory;
    }

    /**
     * 最大segment大小(字节)
     *
     * @return 字节数
     */
    public int maxSegmentSize() {
        return maxSegmentSize;
    }

    /**
     * 每条记录的最大大小
     *
     * @return 字节数
     */
    public int maxMessageSize() {
        return maxMessageSize;
    }
}

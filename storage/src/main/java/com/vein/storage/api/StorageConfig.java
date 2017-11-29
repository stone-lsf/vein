package com.vein.storage.api;

/**
 * @author shifeng.luo
 * @version created on 2017/9/28 下午3:41
 */
public class StorageConfig {

    /**
     * 两个相邻索引记录之间间隔的entry数
     */
    private int indexInterval;

    /**
     * 每个段文件的最大大小
     */
    private int maxSegmentSize;

    /**
     * 每条消息的最大大小
     */
    private int maxMessageSize;

    /**
     * 每个索引文件的最大大小
     */
    private int maxIndexFileSize;

    private int flushInterval;

    public int getIndexInterval() {
        return indexInterval;
    }

    public void setIndexInterval(int indexInterval) {
        this.indexInterval = indexInterval;
    }

    public int getMaxSegmentSize() {
        return maxSegmentSize;
    }

    public void setMaxSegmentSize(int maxSegmentSize) {
        this.maxSegmentSize = maxSegmentSize;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public int getMaxIndexFileSize() {
        return maxIndexFileSize;
    }

    public void setMaxIndexFileSize(int maxIndexFileSize) {
        this.maxIndexFileSize = maxIndexFileSize;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public void setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
    }
}

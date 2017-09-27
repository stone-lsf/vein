package com.sm.finance.charge.storage.api.segment;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:31
 */
public interface SegmentAppender extends AutoCloseable {

    /**
     * 获取关联的segment
     *
     * @return segment
     */
    Segment getSegment();

    /**
     * 写入一条记录
     *
     * @param entry 记录
     * @return {@link CompletableFuture}
     */
    CompletableFuture<Boolean> write(Entry entry);

    /**
     * 刷新缓存到磁盘
     *
     * @return {@link SegmentAppender}
     */
    SegmentAppender flush();
}

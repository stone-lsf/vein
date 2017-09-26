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
     * 删除指定文件内偏移处之后的数据
     *
     * @param offset 文件内偏移
     * @return {@link SegmentAppender}
     */
    SegmentAppender truncate(long offset);

    /**
     * 刷新缓存到磁盘
     *
     * @return {@link SegmentAppender}
     */
    SegmentAppender flush();
}

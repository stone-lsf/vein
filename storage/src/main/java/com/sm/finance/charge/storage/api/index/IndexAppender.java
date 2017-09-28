package com.sm.finance.charge.storage.api.index;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:02
 */
public interface IndexAppender extends AutoCloseable{

    IndexFile getIndexFile();

    /**
     * 写入一条记录
     *
     * @param index 记录
     * @return {@link CompletableFuture}
     */
    CompletableFuture<Boolean> write(OffsetIndex index);

    /**
     * 刷新缓存到磁盘
     *
     * @return {@link IndexAppender}
     */
    IndexAppender flush();
}

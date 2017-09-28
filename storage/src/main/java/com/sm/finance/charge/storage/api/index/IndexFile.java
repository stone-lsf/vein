package com.sm.finance.charge.storage.api.index;

import com.sm.finance.charge.storage.api.segment.Entry;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:54
 */
public interface IndexFile extends AutoCloseable {

    /**
     * 第一条记录索引的{@link Entry}的sequence
     *
     * @return 序列号
     */
    long firstSequence();

    /**
     * 最后一条索引记录
     *
     * @return {@link OffsetIndex}索引记录
     */
    OffsetIndex lastIndex();

    /**
     * 返回小于等于entryOffset的最大的{@link OffsetIndex}offset所在索引
     *
     * @param entryOffset 记录偏移
     * @return {@link OffsetIndex}索引记录
     */
    OffsetIndex floorOffset(long entryOffset);

    /**
     * 返回小于等于entryOffset的最大的{@link OffsetIndex}offset所在索引
     *
     * @param sequence 序列号
     * @return {@link OffsetIndex}索引记录
     */
    OffsetIndex floorSequence(long sequence);

    /**
     * 获取文件
     *
     * @return 文件
     */
    File getFile();

    /**
     * 校验文件是否完整，或者被非法修改过
     *
     * @return 完整数据截止处
     */
    long check() throws IOException;

    /**
     * 删除指定文件内偏移处之后的数据
     *
     * @param offset 文件内偏移
     * @return {@link IndexFile}
     */
    IndexFile truncate(long offset);

    /**
     * 删除指定索引只收的所有数据
     *
     * @param index 索引
     * @return {@link IndexFile}
     */
    IndexFile truncate(OffsetIndex index);

    /**
     * 读取记录
     *
     * @return 记录
     */
    OffsetIndex readIndex();

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
     * @return {@link IndexFile}
     */
    IndexFile flush();
}

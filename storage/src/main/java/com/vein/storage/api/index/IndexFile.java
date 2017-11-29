package com.vein.storage.api.index;

import com.vein.storage.api.segment.Entry;

import java.io.File;
import java.io.IOException;

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
    long baseSequence();

    /**
     * 最后一条索引记录
     *
     * @return {@link OffsetIndex}索引记录
     */
    OffsetIndex lastIndex();

    /**
     * 返回小于等于entryOffset的最大的{@link OffsetIndex}offset所在索引
     *
     * @param sequence 序列号
     * @return {@link OffsetIndex}索引记录
     */
    OffsetIndex lookup(long sequence);

    /**
     * 获取文件
     *
     * @return 文件
     */
    File getFile();

    /**
     * 删除指定文件内偏移处之后的数据
     *
     * @param offset 文件内偏移
     * @return {@link IndexFile}
     */
    IndexFile truncate(long offset) throws IOException;

    /**
     * 收到新增entry记录的sequence和offset
     *
     * @param sequence 记录
     * @param offset   偏移
     */
    void receiveEntry(long sequence, long offset);

    void trimToValidSize() throws IOException;

    /**
     * 刷新缓存到磁盘
     *
     * @return {@link IndexFile}
     */
    IndexFile flush();


    int maxFileSize();

    boolean isFull();

    boolean delete();
}

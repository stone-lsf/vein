package com.sm.finance.charge.storage.api.index;

import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;

import java.io.File;
import java.io.IOException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:54
 */
public interface IndexFile {

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
     * @return {@link Segment}
     */
    IndexFile truncate(long offset);

    /**
     * segment读取器
     *
     * @return 读取器
     */
    IndexReader reader() throws IOException;

    /**
     * segment写入器
     *
     * @return 写入器
     */
    IndexAppender appender() throws IOException;
}

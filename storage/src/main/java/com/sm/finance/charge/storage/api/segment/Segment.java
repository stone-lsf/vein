package com.sm.finance.charge.storage.api.segment;

import com.sm.finance.charge.storage.api.index.IndexFile;

import java.io.File;
import java.io.IOException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:27
 */
public interface Segment {

    /**
     * the sequence of segment first entry
     *
     * @return the segment sequence
     */
    SegmentDescriptor descriptor();

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
    Segment truncate(long offset);

    /**
     * segment读取器
     *
     * @return 读取器
     */
    SegmentReader reader() throws IOException;

    /**
     * segment写入器
     *
     * @return 写入器
     */
    SegmentAppender appender() throws IOException;
}

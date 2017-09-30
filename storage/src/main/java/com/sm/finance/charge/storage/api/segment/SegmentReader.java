package com.sm.finance.charge.storage.api.segment;

import com.sm.finance.charge.storage.api.exceptions.BadDataException;

import java.io.IOException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:31
 */
public interface SegmentReader extends AutoCloseable {

    /**
     * 获取关联的segment
     *
     * @return segment
     */
    Segment getSegment();

    /**
     * 设置读取起始点
     *
     * @param offset 字节数
     * @return {@link SegmentReader}
     */
    SegmentReader readFrom(long offset) throws IOException;

    /**
     * 读取记录
     *
     * @return 记录
     */
    Entry readEntry() throws IOException, BadDataException;
}

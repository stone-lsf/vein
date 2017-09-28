package com.sm.finance.charge.storage.api.segment;

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
     * 跳过bytes字节数
     *
     * @param bytes 字节数
     * @return {@link SegmentReader}
     */
    SegmentReader skip(long bytes);

    /**
     * 读取记录
     *
     * @return 记录
     */
    Entry readEntry();
}

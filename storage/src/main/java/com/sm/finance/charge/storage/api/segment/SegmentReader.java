package com.sm.finance.charge.storage.api.segment;

import com.sm.finance.charge.storage.api.exceptions.BadEntryException;

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
     * 剩余未读取的字节数
     *
     * @return 字节数
     */
    long remaining();

    /**
     * 是否有未读取的数据
     *
     * @return 有则返回true，否则返回false
     */
    boolean hasRemaining();

    /**
     * 跳过bytes字节数
     *
     * @param bytes 字节数
     * @return {@link SegmentReader}
     */
    SegmentReader skip(long bytes);

    /**
     * 从指定位置开始读取
     *
     * @param position 文件内偏移
     * @return {@link SegmentReader}
     */
    SegmentReader position(long position);

    /**
     * 当前偏移位置
     *
     * @return 偏移
     */
    long position();

    /**
     * 读取记录
     *
     * @return 记录
     */
    Entry readEntry() throws BadEntryException;
}

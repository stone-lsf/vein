package com.sm.finance.charge.storage.api.segment;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 下午2:01
 */
public interface SegmentDescriptor {

    /**
     * the sequence of segment first entry
     *
     * @return the segment sequence
     */
    long sequence();

    /**
     * 是否已经被锁定
     *
     * @return 被锁定则返回true，否则返回false
     */
    boolean locked();

    /**
     * 是否已经编辑过
     *
     * @return 编辑过则返回true，否则返回false
     */
    boolean updated();
}

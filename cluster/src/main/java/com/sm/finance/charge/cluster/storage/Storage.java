package com.sm.finance.charge.cluster.storage;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午11:29
 */
public interface Storage {

    /**
     * 返回存储目录
     *
     * @return 目录
     */
    File directory();

    /**
     * 最大segment大小(字节)
     *
     * @return 字节数
     */
    int maxSegmentSize();

    /**
     * appender
     *
     * @return appender
     */
    RollingAppender appender();

    /**
     * rolling reader
     *
     * @return rolling reader
     */
    RollingReader reader();
}

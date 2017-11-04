package com.sm.finance.charge.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午6:39
 */
public class ThreadUtil {
    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);

    /**
     * 可中断的睡眠
     *
     * @param mills 睡眠毫秒数
     */
    public static void sleepInterrupted(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            logger.warn("sleep {}ms is interrupted by exception:{}", mills, e);
        }
    }

    /**
     * 不可中断的睡眠
     *
     * @param mills 睡眠毫秒数
     */
    public static void sleepUnInterrupted(long mills) {
        long start = System.currentTimeMillis();
        long now = start;
        while (now - start < mills) {
            long timeToSleep = mills - (now - start);
            try {
                Thread.sleep(timeToSleep);
            } catch (InterruptedException e) {
                logger.warn("sleep {}ms is interrupted by exception:{}", mills, e);
            }
            now = System.currentTimeMillis();
        }
    }

}

package com.sm.finance.charge.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午6:39
 */
public class ThreadUtil {
    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);

    public static void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            logger.warn("sleep {}ms is interrupted by exception:{}", mills, e);
        }
    }
}

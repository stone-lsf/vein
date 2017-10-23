package com.sm.finance.charge.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午7:39
 */
public class IoUtil {
    private static final Logger logger = LoggerFactory.getLogger(IoUtil.class);

    public static void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.error("close error", e);
            }
        }
    }
}

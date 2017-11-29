package com.vein.common.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:46
 */
public abstract class LoggerSupport {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
}

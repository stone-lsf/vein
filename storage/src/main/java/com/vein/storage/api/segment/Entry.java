package com.vein.storage.api.segment;

import com.vein.storage.api.CheckSum;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:21
 */
public interface Entry extends ReadWritable, CheckSum {

    /**
     * 首部
     *
     * @return {@link Header}首部
     */
    Header head();

    /**
     * 负载
     *
     * @return 字节数组
     */
    byte[] payload();

}

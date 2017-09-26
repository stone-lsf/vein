package com.sm.finance.charge.storage.api.segment;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:21
 */
public interface Entry extends ReadWritable{

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

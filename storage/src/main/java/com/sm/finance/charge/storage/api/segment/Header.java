package com.sm.finance.charge.storage.api.segment;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:47
 */
public interface Header extends ReadWritable {

    /**
     * 序号
     *
     * @return 递增序号
     */
    long sequence();

    /**
     * 消息大小
     *
     * @return 字节数
     */
    int entrySize();

    /**
     * 消息首部大小
     *
     * @return 字节数
     */
    int headerSize();

    /**
     * 32位循环校验码
     *
     * @return 校验码
     */
    int crc32();

    /**
     * 版本号
     *
     * @return 版本号
     */
    byte version();

    /**
     * 压缩算法标识
     *
     * @return 压缩算法标识
     */
    byte compress();

    /**
     * 扩展字节数组
     *
     * @return 数组
     */
    byte[] extend();

}

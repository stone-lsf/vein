package com.sm.finance.charge.storage.api.index;

import com.sm.finance.charge.storage.api.CheckSum;
import com.sm.finance.charge.storage.api.segment.ReadWritable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:13
 */
public interface OffsetIndex extends ReadWritable, CheckSum {

    /**
     * entry序号
     *
     * @return entry {@link com.sm.finance.charge.storage.api.segment.Entry}序号
     */
    long sequence();

    /**
     * segment中的偏移
     *
     * @return {@link com.sm.finance.charge.storage.api.segment.Segment}文件中的偏移
     */
    long offset();

    /**
     * 32位循环校验码
     *
     * @return 校验码
     */
    int crc32();
}

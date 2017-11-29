package com.vein.storage.api.index;

import com.vein.storage.api.CheckSum;
import com.vein.storage.api.segment.Entry;
import com.vein.storage.api.segment.ReadWritable;
import com.vein.storage.api.segment.Segment;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:13
 */
public interface OffsetIndex extends ReadWritable, CheckSum {

    /**
     * entry序号
     *
     * @return entry {@link Entry}序号
     */
    long sequence();

    /**
     * segment中的偏移
     *
     * @return {@link Segment}文件中的偏移
     */
    long offset();

    /**
     * 32位循环校验码
     *
     * @return 校验码
     */
    int crc32();
}

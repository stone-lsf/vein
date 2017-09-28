package com.sm.finance.charge.storage.api.index;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:02
 */
public interface IndexReader extends AutoCloseable {

    IndexFile getIndexFile();

    /**
     * 读取记录
     *
     * @return 记录
     */
    OffsetIndex readIndex();
}

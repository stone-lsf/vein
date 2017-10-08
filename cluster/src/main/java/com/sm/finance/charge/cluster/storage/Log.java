package com.sm.finance.charge.cluster.storage;


/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午3:40
 */
public interface Log {


    long append(Entry entry);

    long firstIndex();

    long lastIndex();

    Entry get(long index);

    Entry lastEntry();

    /**
     * truncate所有大于index的日志
     *
     * @param index 指定日志的索引
     */
    Log truncate(long index);


    Log commit(long index);
}

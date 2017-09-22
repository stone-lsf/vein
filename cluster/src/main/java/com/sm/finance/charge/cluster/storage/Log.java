package com.sm.finance.charge.cluster.storage;

import com.sm.finance.charge.cluster.storage.entry.Entry;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午3:40
 */
public interface Log {


    long append(Entry entry);

    long firstIndex();

    long lastIndex();

    Entry get(long index);
}

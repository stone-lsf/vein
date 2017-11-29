package com.vein.cluster;

import com.vein.cluster.group.Entry;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午11:00
 */
public interface Store {

    long add(Entry entry);

    long lastIndex();

    void skip(long size);

    Entry get(long index);

    void commit(long index);

    void truncate(long index);
}

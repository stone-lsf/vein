package com.sm.charge.cluster;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午11:00
 */
public interface Store<T> {

    long add(BaseObject<T> obj);

    long lastIndex();

    void skip(long size);

    BaseObject<T> get(long index);

    void commit(long index);

    void truncate(long index);
}

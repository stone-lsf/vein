package com.sm.charge.cluster.group;

import com.sm.charge.cluster.BaseObject;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 上午12:20
 */
public class Entry<T> {

    private long index;

    private BaseObject<T> object;

    public Entry() {
    }

    public Entry(long index, BaseObject<T> object) {
        this.index = index;
        this.object = object;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public BaseObject<T> getObject() {
        return object;
    }

    public void setObject(BaseObject<T> object) {
        this.object = object;
    }
}

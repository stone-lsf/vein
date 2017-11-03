package com.sm.charge.cluster;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午11:02
 */
public class BaseObject<T> {

    private T id;

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }
}

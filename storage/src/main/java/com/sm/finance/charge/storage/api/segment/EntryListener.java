package com.sm.finance.charge.storage.api.segment;

/**
 * @author shifeng.luo
 * @version created on 2017/9/29 下午4:42
 */
public interface EntryListener {

    void onCreate(long sequence, long offset);
}

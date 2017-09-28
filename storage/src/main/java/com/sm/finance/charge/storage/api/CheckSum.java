package com.sm.finance.charge.storage.api;

import com.sm.finance.charge.storage.api.exceptions.BadDataException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/28 下午4:11
 */
public interface CheckSum {

    int calculate();

    void check() throws BadDataException;
}

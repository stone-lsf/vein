package com.vein.storage.api;

import com.vein.storage.api.exceptions.BadDataException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/28 下午4:11
 */
public interface CheckSum {

    int buildCheckSum();

    void validCheckSum() throws BadDataException;
}

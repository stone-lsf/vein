package com.sm.finance.charge.storage.api;

import com.sm.finance.charge.common.exceptions.BadDiskException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/30 下午5:10
 */
public interface ExceptionHandler {

    void onBadDiskException(BadDiskException e);
}

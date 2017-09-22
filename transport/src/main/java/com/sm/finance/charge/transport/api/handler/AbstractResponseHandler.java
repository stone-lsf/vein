package com.sm.finance.charge.transport.api.handler;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.transport.api.support.ResponseContext;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午4:55
 */
public abstract class AbstractResponseHandler<T> extends LogSupport implements ResponseHandler<T> {

    @Override
    public void onException(Exception e, ResponseContext context) {
    }

}
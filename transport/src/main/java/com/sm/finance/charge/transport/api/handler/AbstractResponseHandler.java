package com.sm.finance.charge.transport.api.handler;

import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.support.ResponseContext;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午4:55
 */
public abstract class AbstractResponseHandler<T> extends LoggerSupport implements ResponseHandler<T> {

    @Override
    public void handle(T response, Connection connection) {

    }

    @Override
    public void onException(Throwable e, ResponseContext context) {
    }

}

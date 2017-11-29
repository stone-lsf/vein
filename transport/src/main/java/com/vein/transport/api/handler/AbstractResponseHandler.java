package com.vein.transport.api.handler;

import com.vein.common.base.LoggerSupport;
import com.vein.transport.api.Connection;
import com.vein.transport.api.support.ResponseContext;

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

package com.vein.transport.api.handler;

import com.vein.common.base.LoggerSupport;
import com.vein.transport.api.exceptions.RemoteException;
import com.vein.transport.api.exceptions.TimeoutException;
import com.vein.transport.api.support.ResponseContext;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午4:55
 */
public abstract class AbstractExceptionsHandler<T> extends LoggerSupport implements ResponseHandler<T> {

    @Override
    public void onException(Throwable e, ResponseContext context) {
        if (e instanceof RemoteException) {
            onRemoteException((RemoteException) e, context);
        } else if (e instanceof TimeoutException) {
            onTimeoutException((TimeoutException) e, context);
        } else {
            logger.error("received unknown exception", e);
            throw new IllegalStateException("received unknown exception", e);
        }
    }

    protected abstract void onRemoteException(RemoteException e, ResponseContext context);

    protected abstract void onTimeoutException(TimeoutException e, ResponseContext context);
}

package com.sm.finance.charge.transport.api.handler;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.transport.api.exceptions.RemoteException;
import com.sm.finance.charge.transport.api.exceptions.TimeoutException;
import com.sm.finance.charge.transport.api.support.ResponseContext;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午4:55
 */
public abstract class AbstractExceptionResponseHandler<T> extends LogSupport implements ResponseHandler<T> {

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

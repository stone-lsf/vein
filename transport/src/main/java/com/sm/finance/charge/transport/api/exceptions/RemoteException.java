package com.sm.finance.charge.transport.api.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:03
 */
public class RemoteException extends RuntimeException {

    public RemoteException() {
    }

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteException(Throwable cause) {
        super(cause);
    }

    public RemoteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

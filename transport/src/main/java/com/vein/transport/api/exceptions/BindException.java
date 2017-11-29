package com.vein.transport.api.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:21
 */
public class BindException extends Exception {

    public BindException() {
    }

    public BindException(String message) {
        super(message);
    }

    public BindException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindException(Throwable cause) {
        super(cause);
    }

    public BindException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

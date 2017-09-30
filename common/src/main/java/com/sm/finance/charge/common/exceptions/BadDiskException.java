package com.sm.finance.charge.common.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/28 下午2:18
 */
public class BadDiskException extends Exception {

    public BadDiskException() {
    }

    public BadDiskException(String message) {
        super(message);
    }

    public BadDiskException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadDiskException(Throwable cause) {
        super(cause);
    }

    public BadDiskException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

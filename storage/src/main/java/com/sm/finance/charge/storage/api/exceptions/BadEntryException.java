package com.sm.finance.charge.storage.api.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 下午11:37
 */
public class BadEntryException extends Exception {
    public BadEntryException() {
    }

    public BadEntryException(String message) {
        super(message);
    }

    public BadEntryException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadEntryException(Throwable cause) {
        super(cause);
    }

    public BadEntryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

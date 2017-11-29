package com.vein.storage.api.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 下午11:37
 */
public class BadDataException extends RuntimeException {
    public BadDataException() {
    }

    public BadDataException(String message) {
        super(message);
    }

    public BadDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadDataException(Throwable cause) {
        super(cause);
    }

    public BadDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

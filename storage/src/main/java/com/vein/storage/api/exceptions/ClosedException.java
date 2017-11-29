package com.vein.storage.api.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/28 上午12:12
 */
public class ClosedException extends RuntimeException {
    public ClosedException() {
    }

    public ClosedException(String message) {
        super(message);
    }

    public ClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClosedException(Throwable cause) {
        super(cause);
    }

    public ClosedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

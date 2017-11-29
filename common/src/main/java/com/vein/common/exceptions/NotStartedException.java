package com.vein.common.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/28 上午12:12
 */
public class NotStartedException extends RuntimeException {
    public NotStartedException() {
    }

    public NotStartedException(String message) {
        super(message);
    }

    public NotStartedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotStartedException(Throwable cause) {
        super(cause);
    }

    public NotStartedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

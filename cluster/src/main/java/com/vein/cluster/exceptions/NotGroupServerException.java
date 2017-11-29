package com.vein.cluster.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午9:46
 */
public class NotGroupServerException extends RuntimeException{

    public NotGroupServerException() {
    }

    public NotGroupServerException(String message) {
        super(message);
    }

    public NotGroupServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotGroupServerException(Throwable cause) {
        super(cause);
    }

    public NotGroupServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

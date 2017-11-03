package com.sm.charge.cluster.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/11/3 下午5:20
 */
public class NoGroupException extends RuntimeException {

    public NoGroupException() {
    }

    public NoGroupException(String message) {
        super(message);
    }

    public NoGroupException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoGroupException(Throwable cause) {
        super(cause);
    }

    public NoGroupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

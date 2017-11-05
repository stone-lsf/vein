package com.sm.finance.charge.common.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午10:20
 */
public class MessageOverLimitException extends RuntimeException {
    public MessageOverLimitException() {
    }

    public MessageOverLimitException(String message) {
        super(message);
    }

    public MessageOverLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageOverLimitException(Throwable cause) {
        super(cause);
    }

    public MessageOverLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

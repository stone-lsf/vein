package com.sm.finance.charge.transport.api.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:04
 */
public class TimeoutException extends RuntimeException {

    private final int timeout;

    public TimeoutException(int timeout) {
        this.timeout = timeout;
    }

    public TimeoutException(String message, int timeout) {
        super(message);
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "TimeoutException{" +
            "timeout=" + timeout +
            '}';
    }
}

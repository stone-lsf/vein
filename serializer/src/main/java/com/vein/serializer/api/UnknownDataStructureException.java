package com.vein.serializer.api;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:40
 */
public class UnknownDataStructureException extends RuntimeException {
    public UnknownDataStructureException() {
    }

    public UnknownDataStructureException(String message) {
        super(message);
    }

    public UnknownDataStructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownDataStructureException(Throwable cause) {
        super(cause);
    }

    public UnknownDataStructureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

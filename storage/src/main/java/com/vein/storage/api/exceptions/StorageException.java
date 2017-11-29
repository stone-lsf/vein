package com.vein.storage.api.exceptions;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 下午10:56
 */
public class StorageException extends RuntimeException {
    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }
}

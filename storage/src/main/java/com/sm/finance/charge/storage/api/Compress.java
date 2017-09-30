package com.sm.finance.charge.storage.api;

/**
 * @author shifeng.luo
 * @version created on 2017/9/29 下午2:47
 */
public enum Compress {
    NONE((byte) 0);

    public final byte code;

    Compress(byte code) {
        this.code = code;
    }
}

package com.sm.finance.charge.common;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shifeng.luo
 * @version created on 2017/9/29 上午12:03
 */
public class LongIdGenerator {

    private AtomicLong count = new AtomicLong(0);

    public LongIdGenerator() {
    }

    public LongIdGenerator(long initId) {
        count.set(initId);
    }

    public long nextId() {
        return count.incrementAndGet();
    }

    public long current() {
        return count.get();
    }

    public long preId() {
        return current() - 1;
    }
}

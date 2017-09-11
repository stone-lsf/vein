package com.sm.finance.charge.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:54
 */
public class IntegerIdGenerator {

    private AtomicInteger count = new AtomicInteger(0);

    public IntegerIdGenerator() {
    }

    public IntegerIdGenerator(int initId) {
        count.set(initId);
    }

    public int nextId() {
        return count.incrementAndGet();
    }

    public int current() {
        return count.get();
    }

    public int preId() {
        return current() - 1;
    }
}

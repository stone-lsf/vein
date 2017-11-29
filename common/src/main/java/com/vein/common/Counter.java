package com.vein.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 计数器
 *
 * @author shifeng.luo
 * @version created on 2017/9/18 下午5:12
 */
public class Counter {
    private AtomicInteger count = new AtomicInteger(0);
    private final Listener listener;

    public Counter(Listener listener) {
        this.listener = listener;
    }

    public void increase() {
        int num = count.incrementAndGet();
        listener.onCount(num);
    }

    public int getCount() {
        return count.get();
    }

    public interface Listener {
        void onCount(int count);
    }
}

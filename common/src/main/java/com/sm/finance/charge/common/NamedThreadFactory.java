package com.sm.finance.charge.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午6:11
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolSequence = new AtomicInteger(1);
    private final AtomicInteger threadNum = new AtomicInteger(1);
    private final String prefix;
    private final boolean daemon;
    private final ThreadGroup group;

    public NamedThreadFactory(String poolPrefix) {
        this(poolPrefix, false);
    }

    public NamedThreadFactory(String poolPrefix, boolean daemon) {
        this.prefix = poolPrefix + "-" + poolSequence.getAndIncrement();
        this.daemon = daemon;

        SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager == null ? Thread.currentThread().getThreadGroup() : securityManager.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        String name = prefix + "-" + this.threadNum.getAndIncrement();
        Thread thread = new Thread(group, runnable, name);
        thread.setDaemon(daemon);
        return thread;
    }
}

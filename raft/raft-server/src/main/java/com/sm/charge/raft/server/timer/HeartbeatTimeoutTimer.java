package com.sm.charge.raft.server.timer;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午3:40
 */
public class HeartbeatTimeoutTimer extends AbstractRaftTimer {

    /**
     * 定时间隔
     */
    private final int interval;

    public HeartbeatTimeoutTimer(int interval, ScheduledExecutorService executor) {
        super(executor);
        this.interval = interval;
    }

    @Override
    public void run() {

    }

    @Override
    public int interval() {
        return interval;
    }
}

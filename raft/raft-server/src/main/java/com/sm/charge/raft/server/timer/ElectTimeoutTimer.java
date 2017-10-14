package com.sm.charge.raft.server.timer;

import com.sm.finance.charge.common.RandomUtil;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午3:59
 */
public class ElectTimeoutTimer extends AbstractRaftTimer {

    /**
     * 最大选举超时时间
     */
    private final int maxInterval;
    /**
     * 最小选举超时时间
     */
    private final int minInterval;

    public ElectTimeoutTimer(ScheduledExecutorService executor, int maxInterval, int minInterval) {
        super(executor);
        this.maxInterval = maxInterval;
        this.minInterval = minInterval;
    }


    @Override
    public void run() {

    }

    @Override
    public int interval() {
        return RandomUtil.between(minInterval, maxInterval);
    }
}

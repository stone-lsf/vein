package com.vein.raft.server.state.support.timer;

import com.vein.raft.server.ServerContext;

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
    private final ServerContext context;

    public HeartbeatTimeoutTimer(int interval, ScheduledExecutorService executor, ServerContext context) {
        super(executor);
        this.interval = interval;
        this.context = context;
    }

    @Override
    public void run() {
        logger.info("heartbeat timeout,interval:{}", interval);
        try {
            context.onHeartbeatTimeout();
        } catch (Throwable error) {
            logger.error("heartbeat timer caught error", error);
        }
    }

    @Override
    public int interval() {
        return interval;
    }
}

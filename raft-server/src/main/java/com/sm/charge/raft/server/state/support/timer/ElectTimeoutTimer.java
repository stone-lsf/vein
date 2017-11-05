package com.sm.charge.raft.server.state.support.timer;

import com.sm.charge.raft.server.ServerContext;
import com.sm.finance.charge.common.utils.RandomUtil;

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

    private final ServerContext context;

    public ElectTimeoutTimer(ScheduledExecutorService executor, int maxInterval, int minInterval, ServerContext context) {
        super(executor);
        this.maxInterval = maxInterval;
        this.minInterval = minInterval;
        this.context = context;
    }


    @Override
    public void run() {
        logger.info("elect timeout,max:{},min:{}", maxInterval, minInterval);

        try {
            context.onElectTimeout();
        } catch (Throwable error) {
            logger.error("elect timer caught error", error);
        }
    }

    @Override
    public int interval() {
        return RandomUtil.between(minInterval, maxInterval);
    }
}

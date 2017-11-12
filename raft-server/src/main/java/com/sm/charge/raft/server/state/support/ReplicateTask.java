package com.sm.charge.raft.server.state.support;

import com.sm.charge.raft.server.RaftMember;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.common.SystemConstants;
import com.sm.finance.charge.common.base.LoggerSupport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author shifeng.luo
 * @version created on 2017/10/18 下午2:01
 */
public class ReplicateTask extends LoggerSupport {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(SystemConstants.PROCESSORS, new NamedThreadFactory("AppendPoll"));
    private volatile boolean started = false;
    private Future<?> future;
    private final RaftMember target;
    private final Replicator replicator;

    public ReplicateTask(RaftMember target, Replicator replicator) {
        this.target = target;
        this.replicator = replicator;
    }

    public synchronized void start() {
        if (!started) {
            future = EXECUTOR.submit(this::execute);
            started = true;
        }
    }

    private void execute() {
        replicator.replicateTo(target).whenComplete((result, error) -> {
            if (error != null) {
                logger.error("replicate entry to {} caught exception", target.getNodeId(), error);
            }

            if (started) {
                future = EXECUTOR.submit(this::execute);
            }
        });
    }


    public synchronized void stop() {
        if (started) {
            if (future != null) {
                future.cancel(false);
            }
            started = false;
        }
    }
}

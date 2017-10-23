package com.sm.charge.raft.server.replicate;

import com.sm.charge.raft.server.RaftMember;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.common.SystemConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shifeng.luo
 * @version created on 2017/10/18 下午2:01
 */
public class ReplicateTask extends LoggerSupport {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(SystemConstants.PROCESSORS, new NamedThreadFactory("AppendPoll"));
    private volatile boolean started = false;
    private final RaftMember target;
    private final Replicator replicator;

    public ReplicateTask(RaftMember target, Replicator replicator) {
        this.target = target;
        this.replicator = replicator;
    }

    public void start() {
        started = true;
        EXECUTOR.execute(this::execute);
    }

    private void execute() {
        replicator.replicateTo(target).whenComplete((result, error) -> {
            if (error != null) {
                logger.error("replicate entry to {} caught exception", target.getId(), error);
            }

            if (started) {
                EXECUTOR.execute(this::execute);
            }
        });
    }


    public void stop() {
        started = false;
    }
}

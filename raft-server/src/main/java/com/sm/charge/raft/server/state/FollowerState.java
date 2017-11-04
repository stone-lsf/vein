package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftListener;
import com.sm.charge.raft.server.RaftState;
import com.sm.charge.raft.server.ServerContext;
import com.sm.charge.raft.server.replicate.AppendRequest;
import com.sm.charge.raft.server.replicate.AppendResponse;
import com.sm.charge.raft.server.timer.HeartbeatTimeoutTimer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:50
 */
public class FollowerState extends AbstractState {

    private final HeartbeatTimeoutTimer timer;

    public FollowerState(RaftListener raftListener, ServerContext context, HeartbeatTimeoutTimer timer) {
        super(raftListener, context);
        this.timer = timer;
    }

    @Override
    public RaftState state() {
        return RaftState.FOLLOWER;
    }

    @Override
    protected AppendResponse doHandle(AppendRequest request) {
        timer.reset();
        return super.doHandle(request);
    }

    @Override
    public void suspect() {
        timer.stop();
    }

    @Override
    public void wakeup() {
        timer.start();
    }

}

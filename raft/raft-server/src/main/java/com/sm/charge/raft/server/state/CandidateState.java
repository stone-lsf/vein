package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftListener;
import com.sm.charge.raft.server.RaftState;
import com.sm.charge.raft.server.ServerContext;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:50
 */
public class CandidateState extends AbstractState {


    public CandidateState(RaftListener raftListener, ServerContext context) {
        super(raftListener, context);
    }

    @Override
    public RaftState state() {
        return RaftState.CANDIDATE;
    }

    @Override
    public void suspect() {

    }

    @Override
    public void wakeup() {

    }
}

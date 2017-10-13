package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftMemberState;
import com.sm.charge.raft.server.RaftMemberStateManager;
import com.sm.charge.raft.server.RaftStateMachine;
import com.sm.charge.raft.server.ServerStateMachine;
import com.sm.charge.raft.server.storage.Log;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:50
 */
public class CandidateState extends AbstractState {
    public CandidateState(RaftMemberState memberState, RaftStateMachine stateMachine, Log log, RaftMemberStateManager stateManager, ServerStateMachine serverStateMachine) {
        super(memberState, stateMachine, log, stateManager, serverStateMachine);
    }

}

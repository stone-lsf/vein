package com.sm.charge.raft.server.state;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:50
 */
public class CandidateState extends AbstractState{
    @Override
    public String name() {
        return "candidate";
    }
}

package com.sm.charge.raft.server.state;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:50
 */
public class LeaderState extends AbstractState {
    @Override
    public String name() {
        return "leader";
    }
}
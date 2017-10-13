package com.sm.charge.raft.server;

/**
 * @author shifeng.luo
 * @version created on 2017/10/13 下午10:54
 */
public interface RaftMemberStateManager {

    /**
     * 持久化server state
     *
     * @param memberState server state
     */
    void persistState(RaftMemberState memberState);
}

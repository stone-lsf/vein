package com.sm.charge.raft.server.storage;

import com.sm.charge.raft.server.RaftMember;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午1:11
 */
public interface MemberStateManager {

    /**
     * 持久化server state
     *
     * @param memberState server state
     */
    void persistState(RaftMember memberState);
}

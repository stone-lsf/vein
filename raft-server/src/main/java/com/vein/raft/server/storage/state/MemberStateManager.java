package com.vein.raft.server.storage.state;

import com.vein.raft.server.RaftMember;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午1:11
 */
public interface MemberStateManager {

    /**
     * 持久化server state
     *
     * @param member {@link RaftMember}
     */
    void persistState(RaftMember member);

    /**
     * 加载磁盘上的server state
     *
     * @return {@link RaftMember}
     */
    RaftMember loadState();
}

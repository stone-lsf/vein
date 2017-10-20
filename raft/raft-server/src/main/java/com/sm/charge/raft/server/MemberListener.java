package com.sm.charge.raft.server;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:30
 */
public interface MemberListener {

    void onJoin(RaftMemberState member);

    void onLeave(RaftMemberState member);
}

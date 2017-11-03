package com.sm.charge.raft.server;

import com.sm.charge.raft.server.election.MasterListener;
import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:36
 */
public interface RaftCluster extends Startable, Closable {

    String name();

    RaftMember master();

    void setMaster(RaftMember master);

    RaftMember member(String memberId);

    boolean contain(String memberId);

    RaftMember local();

    int getQuorum();

    long version();

    void setVersion(long version);

    List<RaftMember> members();

    void add(RaftMember member);

    void remove(RaftMember member);

    void addMemberListener(MemberListener listener);

    void addMasterListener(MasterListener listener);
}

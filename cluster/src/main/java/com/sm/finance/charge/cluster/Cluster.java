package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.elect.MasterListener;
import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:36
 */
public interface Cluster extends Startable, Closable {

    String name();

    ClusterMember master();

    void setMaster(ClusterMember master);

    ClusterMember member(long id);

    ClusterMember local();

    int getQuorum();

    long version();

    void setVersion(long version);

    List<ClusterMember> members();

    void add(ClusterMember member);

    void remove(ClusterMember member);

    void addMemberListener(MemberListener listener);

    void addMasterListener(MasterListener listener);
}

package com.sm.finance.charge.cluster;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:30
 */
public interface MemberListener {

    void onJoin(ClusterMember member);

    void onLeave(ClusterMember member);
}

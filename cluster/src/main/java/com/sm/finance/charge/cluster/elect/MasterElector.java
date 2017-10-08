package com.sm.finance.charge.cluster.elect;

import com.sm.finance.charge.cluster.ClusterMember;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/13 上午12:04
 */
public interface MasterElector {

    /**
     * 从节点列表中选出master
     *
     * @param members 节点列表
     * @return master
     */
    ClusterMember elect(List<ClusterMember> members);
}

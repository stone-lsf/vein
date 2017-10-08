package com.sm.finance.charge.cluster.elect;

import com.sm.finance.charge.cluster.ClusterMember;
import com.sm.finance.charge.cluster.ClusterMemberState;
import com.sm.finance.charge.cluster.storage.Log;

import java.util.Comparator;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:29
 */
public class DefaultMasterElector implements MasterElector {

    private volatile int minCandidateNum;
    private final Log log;

    public DefaultMasterElector(int minCandidateNum, Log log) {
        this.minCandidateNum = minCandidateNum;
        this.log = log;
    }

    @Override
    public ClusterMember elect(List<ClusterMember> members) {
        if (members.size() < minCandidateNum) {
            return null;
        }



        return members.get(0);
    }



}

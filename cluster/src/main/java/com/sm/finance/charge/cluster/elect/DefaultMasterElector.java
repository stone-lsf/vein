package com.sm.finance.charge.cluster.elect;

import com.sm.finance.charge.cluster.discovery.DiscoveryNode;

import java.util.Comparator;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:29
 */
public class DefaultMasterElector implements MasterElector {

    private volatile int minCandidateNum;

    public DefaultMasterElector(int minCandidateNum) {
        this.minCandidateNum = minCandidateNum;
    }

    @Override
    public DiscoveryNode elect(List<DiscoveryNode> nodes) {
        if (nodes.size() < minCandidateNum) {
            return null;
        }

        nodes.sort(new NodeComparator());

        return nodes.get(0);
    }


    private class NodeComparator implements Comparator<DiscoveryNode> {

        @Override
        public int compare(DiscoveryNode node1, DiscoveryNode node2) {
            return node1.getNodeId().compareToIgnoreCase(node2.getNodeId());
        }
    }
}

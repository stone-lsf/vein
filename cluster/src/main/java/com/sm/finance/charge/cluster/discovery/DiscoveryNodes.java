package com.sm.finance.charge.cluster.discovery;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.sm.finance.charge.cluster.discovery.pushpull.PushNodeState;

import java.util.List;
import java.util.Map;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:57
 */
public class DiscoveryNodes {

    private Map<String, DiscoveryNode> nodeMap = Maps.newHashMap();
    private List<String> nodeIds = Lists.newArrayList();
    private Map<String, DiscoveryNode> aliveNodes = Maps.newHashMap();
    private Map<String, DiscoveryNode> suspectNodes = Maps.newHashMap();
    private Map<String, DiscoveryNode> deadNodes = Maps.newHashMap();

    private final String localNodeId;

    public DiscoveryNodes(String localNodeId) {
        this.localNodeId = localNodeId;
    }

    public String getLocalNodeId() {
        return localNodeId;
    }

    public synchronized List<PushNodeState> buildPushNodeStates() {
        List<PushNodeState> result = Lists.newArrayListWithCapacity(nodeMap.size());
        nodeMap.values().forEach(node -> result.add(node.toPushNodeState()));
        return result;
    }
}

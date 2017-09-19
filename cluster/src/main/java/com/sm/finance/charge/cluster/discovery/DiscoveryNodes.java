package com.sm.finance.charge.cluster.discovery;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.sm.finance.charge.cluster.discovery.pushpull.PushNodeState;
import com.sm.finance.charge.common.RandomUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:57
 */
public class DiscoveryNodes {

    private List<String> nodeIds = Lists.newArrayList();
    private Map<String, DiscoveryNode> nodeMap = Maps.newHashMap();
    private Map<String, DiscoveryNode> aliveNodes = Maps.newHashMap();
    private Map<String, DiscoveryNode> suspectNodes = Maps.newHashMap();

    private final String localNodeId;

    private final Lock readLock;
    private final Lock writeLock;

    public DiscoveryNodes(String localNodeId) {
        this.localNodeId = localNodeId;

        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public String getLocalNodeId() {
        return localNodeId;
    }

    public DiscoveryNode getLocalNode() {
        return nodeMap.get(localNodeId);
    }

    public boolean isLocalNode(String nodeId) {
        return localNodeId.equals(nodeId);
    }

    public List<PushNodeState> buildPushNodeStates() {
        readLock.lock();
        try {
            List<PushNodeState> result = Lists.newArrayListWithCapacity(nodeMap.size());
            nodeMap.values().forEach(node -> result.add(node.toPushNodeState()));
            return result;
        } finally {
            readLock.unlock();
        }
    }

    public void aliveNode(DiscoveryNode node) {
        writeLock.lock();
        try {
            String nodeId = node.getNodeId();
            aliveNodes.put(nodeId, node);

            suspectNodes.remove(nodeId);
        } finally {
            writeLock.unlock();
        }
    }

    public void suspectNode(DiscoveryNode node) {
        writeLock.lock();
        try {
            String nodeId = node.getNodeId();
            suspectNodes.put(nodeId, node);

            aliveNodes.remove(nodeId);
        } finally {
            writeLock.unlock();
        }
    }

    public void deadNode(DiscoveryNode node) {
        writeLock.lock();
        try {
            String nodeId = node.getNodeId();
            nodeMap.remove(nodeId);
            nodeIds.remove(nodeId);
            aliveNodes.remove(nodeId);
            suspectNodes.remove(nodeId);
        } finally {
            writeLock.unlock();
        }
    }

    public List<DiscoveryNode> randomNodes(int expect, NodeFilter filter) {
        readLock.lock();
        try {
            List<DiscoveryNode> result = Lists.newArrayListWithCapacity(expect);
            Set<String> set = Sets.newHashSetWithExpectedSize(expect);

            List<DiscoveryNode> nodes = Lists.newArrayList(nodeMap.values());
            int size = nodes.size();

            for (int i = 0; i < expect && i < 3 * size; i++) {
                int index = RandomUtil.random(size);
                DiscoveryNode node = nodes.get(index);
                if (set.contains(node.getNodeId())) {
                    continue;
                }

                if (filter != null && filter.apply(node)) {
                    continue;
                }

                result.add(node);
                set.add(node.getNodeId());
            }

            return result;
        } finally {
            readLock.unlock();
        }
    }

    public DiscoveryNode get(String nodeId) {
        readLock.lock();
        try {
            return nodeMap.get(nodeId);
        } finally {
            readLock.unlock();
        }
    }

    public DiscoveryNode get(int index) {
        readLock.lock();
        try {
            if (index >= nodeIds.size()) {
                return null;
            }
            String nodeId = nodeIds.get(index);
            return nodeMap.get(nodeId);
        } finally {
            readLock.unlock();
        }
    }

    public DiscoveryNode addIfAbsent(DiscoveryNode node) {
        writeLock.lock();
        try {
            String nodeId = node.getNodeId();
            DiscoveryNode existNode = nodeMap.get(nodeId);
            if (existNode != null) {
                return existNode;
            }

            nodeMap.put(nodeId, node);
            nodeIds.add(nodeId);
            return null;
        } finally {
            writeLock.unlock();
        }
    }


    public int size() {
        readLock.lock();
        try {
            return nodeMap.size();
        } finally {
            readLock.unlock();
        }
    }
}

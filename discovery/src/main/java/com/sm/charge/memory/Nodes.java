package com.sm.charge.memory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.sm.charge.memory.pushpull.PushNodeState;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.common.utils.ListUtil;
import com.sm.finance.charge.common.utils.RandomUtil;

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
public class Nodes extends LoggerSupport {

    private List<String> nodeIds = Lists.newArrayList();
    private Map<String, Node> nodeMap = Maps.newHashMap();
    private Map<String, Node> aliveNodes = Maps.newHashMap();
    private Map<String, Node> suspectNodes = Maps.newHashMap();
    private Map<String, Node> deadNodes = Maps.newHashMap();

    private final String self;

    private final Lock readLock;
    private final Lock writeLock;

    public Nodes(String self) {
        this.self = self;

        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public String getSelf() {
        return self;
    }

    public Node getLocalNode() {
        return nodeMap.get(self);
    }

    public boolean isLocalNode(String nodeId) {
        return self.equals(nodeId);
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

    public List<Node> getAliveNodes() {
        readLock.lock();
        try {
            return ListUtil.toList(aliveNodes);
        } finally {
            readLock.unlock();
        }
    }

    public List<Node> getSuspectNodes() {
        readLock.lock();
        try {
            return ListUtil.toList(suspectNodes);
        } finally {
            readLock.unlock();
        }
    }

    public List<Node> getDeadNodes() {
        readLock.lock();
        try {
            return ListUtil.toList(deadNodes);
        } finally {
            readLock.unlock();
        }
    }


    public List<Node> getAll() {
        readLock.lock();
        try {
            return ListUtil.toList(nodeMap);
        } finally {
            readLock.unlock();
        }
    }

    public void aliveNode(Node node) {
        writeLock.lock();
        try {
            String nodeId = node.getNodeId();
            aliveNodes.put(nodeId, node);

            suspectNodes.remove(nodeId);
            deadNodes.remove(nodeId);
        } finally {
            writeLock.unlock();
        }
    }

    public void suspectNode(Node node) {
        writeLock.lock();
        try {
            String nodeId = node.getNodeId();
            suspectNodes.put(nodeId, node);

            deadNodes.remove(nodeId);
            aliveNodes.remove(nodeId);
        } finally {
            writeLock.unlock();
        }
    }

    public void deadNode(Node node) {
        writeLock.lock();
        try {
            String nodeId = node.getNodeId();
            aliveNodes.remove(nodeId);
            suspectNodes.remove(nodeId);

            deadNodes.put(nodeId, node);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeDeadNodes() {
        writeLock.lock();
        try {
            for (String nodeId : deadNodes.keySet()) {
                nodeMap.remove(nodeId);
                nodeIds.remove(nodeId);
            }

            deadNodes.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public List<Node> randomNodes(int expect, NodeFilter filter) {
        readLock.lock();
        try {
            List<Node> result = Lists.newArrayListWithCapacity(expect);
            Set<String> set = Sets.newHashSetWithExpectedSize(expect);

            List<Node> nodes = Lists.newArrayList(nodeMap.values());
            int size = nodes.size();

            for (int i = 0, count = 0; i < expect && count < 3 * size; count++) {
                int index = RandomUtil.random(size);
                Node node = nodes.get(index);
                if (set.contains(node.getNodeId())) {
                    continue;
                }

                if (filter != null && filter.apply(node)) {
                    continue;
                }

                result.add(node);
                set.add(node.getNodeId());
                i++;
            }

            return result;
        } finally {
            readLock.unlock();
        }
    }

    public Node get(String nodeId) {
        readLock.lock();
        try {
            return nodeMap.get(nodeId);
        } finally {
            readLock.unlock();
        }
    }

    public Node get(int index) {
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

    public Node addIfAbsent(Node node) {
        writeLock.lock();
        try {
            String nodeId = node.getNodeId();
            Node existNode = nodeMap.get(nodeId);
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

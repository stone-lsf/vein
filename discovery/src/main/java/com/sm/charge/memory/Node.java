package com.sm.charge.memory;

import com.sm.charge.memory.gossip.messages.AliveMessage;
import com.sm.charge.memory.pushpull.PushNodeState;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;

import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:35
 */
public class Node {

    /**
     * 节点唯一标识符
     */
    private final String nodeId;

    /**
     * 节点地址
     */
    private final Address address;

    /**
     * 节点类型
     */
    private final NodeType type;

    /**
     * 节点连接
     */
    private volatile Connection connection;

    /**
     * 自增的版本号
     */
    private volatile long incarnation;

    /**
     * 节点状态
     */
    private volatile NodeStatus status;

    /**
     * 状态发生变化的时间
     */
    private volatile Date statusChangeTime;

    private final ReentrantLock lock = new ReentrantLock();

    public Node(AliveMessage message, NodeStatus status, Date statusChangeTime) {
        this.nodeId = message.getNodeId();
        this.address = message.getAddress();
        this.type = message.getNodeType();
        this.status = status;
        this.statusChangeTime = statusChangeTime;
    }

    public Node(String nodeId, Address address, NodeType type, long incarnation, Date statusChangeTime, NodeStatus status) {
        this.nodeId = nodeId;
        this.address = address;
        this.type = type;
        this.incarnation = incarnation;
        this.statusChangeTime = statusChangeTime;
        this.status = status;
    }

    PushNodeState toPushNodeState() {
        PushNodeState state = new PushNodeState();

        state.setAddress(this.address);
        state.setNodeId(this.nodeId);
        state.setIncarnation(this.incarnation);
        state.setStatus(this.status);
        state.setType(this.type);

        return state;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public String getNodeId() {
        return nodeId;
    }

    public Address getAddress() {
        return address;
    }

    public NodeType getType() {
        return type;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public long nextIncarnation() {
        lock.lock();
        try {
            return ++incarnation;
        } finally {
            lock.unlock();
        }
    }

    public long getIncarnation() {
        return incarnation;
    }

    public void setIncarnation(long incarnation) {
        this.incarnation = incarnation;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public Date getStatusChangeTime() {
        return statusChangeTime;
    }

    public void setStatusChangeTime(Date statusChangeTime) {
        this.statusChangeTime = statusChangeTime;
    }

    @Override
    public String toString() {
        return "DiscoveryNode{" +
            "nodeId='" + nodeId + '\'' +
            ", address=" + address +
            ", type=" + type +
            ", incarnation=" + incarnation +
            ", status=" + status +
            ", statusChangeTime=" + statusChangeTime +
            '}';
    }
}

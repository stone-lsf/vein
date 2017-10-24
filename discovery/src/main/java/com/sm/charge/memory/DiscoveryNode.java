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
public class DiscoveryNode {

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
    private final Type type;

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
    private volatile DiscoveryNode.Status status;

    /**
     * 状态发生变化的时间
     */
    private volatile Date statusChangeTime;

    private final ReentrantLock lock = new ReentrantLock();

    public DiscoveryNode(AliveMessage message, DiscoveryNode.Status status, Date statusChangeTime) {
        this.nodeId = message.getNodeId();
        this.address = message.getAddress();
        this.type = message.getNodeType();
        this.status = status;
        this.statusChangeTime = statusChangeTime;
    }

    public DiscoveryNode(String nodeId, Address address, Type type, long incarnation, Date statusChangeTime, DiscoveryNode.Status status) {
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
        state.setNodeStatus(this.status);
        state.setNodeType(this.type);

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

    public Type getType() {
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getStatusChangeTime() {
        return statusChangeTime;
    }

    public void setStatusChangeTime(Date statusChangeTime) {
        this.statusChangeTime = statusChangeTime;
    }

    public enum Status {
        ALIVE((byte) 1, "alive"),
        SUSPECT((byte) 2, "suspect"),
        DEAD((byte) 3, "dead");

        public final byte code;

        public final String desc;

        Status(byte code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static Status valueOf(byte code) {
            Status[] statuses = Status.values();
            for (Status status : statuses) {
                if (status.code == code) {
                    return status;
                }
            }

            throw new RuntimeException("unknown Status code:" + code);
        }
    }

    public enum Type {
        DATA(1, "数据节点"),
        CANDIDATE(2, "候选节点");

        public final int code;

        public final String desc;

        Type(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static Type valueOf(byte code) {
            Type[] types = Type.values();
            for (Type type : types) {
                if (type.code == code) {
                    return type;
                }
            }

            throw new RuntimeException("unknown Type code:" + code);
        }
    }
}

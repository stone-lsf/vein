package com.sm.finance.charge.cluster.discovery;

import com.sm.finance.charge.cluster.discovery.gossip.messages.AliveMessage;
import com.sm.finance.charge.cluster.discovery.pushpull.PushNodeState;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportServer;

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
     * 节点当前状态
     */
    private final DiscoveryNodeState state;

    /**
     * 节点类型
     */
    private final Type type;

    /**
     * 节点连接
     */
    private volatile Connection connection;

    /**
     * 连接客户端
     */
    private TransportClient transportClient;

    /**
     * 连接服务端
     */
    private TransportServer transportServer;

    private ReentrantLock lock = new ReentrantLock();

    public DiscoveryNode(AliveMessage message, DiscoveryNodeState state) {
        this.nodeId = message.getNodeId();
        this.address = message.getAddress();
        this.type = message.getNodeType();
        this.state = state;
    }

    public DiscoveryNode(String nodeId, Address address, DiscoveryNodeState state, Type type) {
        this.nodeId = nodeId;
        this.address = address;
        this.state = state;
        this.type = type;
    }

    PushNodeState toPushNodeState() {
        PushNodeState state = new PushNodeState();

        state.setAddress(this.address);
        state.setNodeId(this.nodeId);
        state.setIncarnation(this.state.getIncarnation());
        state.setNodeStatus(this.state.getStatus());
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

    public DiscoveryNodeState getState() {
        return state;
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

    public TransportClient getTransportClient() {
        return transportClient;
    }

    public void setTransportClient(TransportClient transportClient) {
        this.transportClient = transportClient;
    }

    public TransportServer getTransportServer() {
        return transportServer;
    }

    public void setTransportServer(TransportServer transportServer) {
        this.transportServer = transportServer;
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

package com.sm.finance.charge.common.base;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/11/3 下午1:58
 */
public class BaseNode<T> {
    /**
     * 节点唯一标识符
     */
    protected final T nodeId;

    protected final Address address;

    public BaseNode(T nodeId, Address address) {
        this.nodeId = nodeId;
        this.address = address;
    }

    public T getNodeId() {
        return nodeId;
    }

    public Address getAddress() {
        return address;
    }
}

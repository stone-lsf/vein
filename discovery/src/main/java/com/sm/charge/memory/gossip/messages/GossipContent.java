package com.sm.charge.memory.gossip.messages;


/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:25
 */
public interface GossipContent {

    short ALIVE = 100;
    short SUSPECT = 101;
    short DEAD = 102;
    short USER = 200;

    /**
     * 获取消息类型
     *
     * @return 类型值
     */
    short getType();

    String getNodeId();

    void setNodeId(String nodeId);

    long getIncarnation();

    void setIncarnation(long incarnation);
}

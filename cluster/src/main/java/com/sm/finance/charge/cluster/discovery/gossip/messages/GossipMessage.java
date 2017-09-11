package com.sm.finance.charge.cluster.discovery.gossip.messages;

import com.sm.finance.charge.cluster.discovery.gossip.GossipFinishNotifier;

/**
 * 在集群中进行传播的消息
 *
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:25
 */
public interface GossipMessage {

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

    /**
     * 是否使消息message失效
     *
     * 当同一个节点的同一类型的GossipMessage，后入队的消息会使之前的消息失效
     *
     * @param message GossipMessage
     * @return 如果使message失效，则返回true，否则返回false
     */
    boolean invalidate(GossipMessage message);

    /**
     * 传播完成时调用本方法
     */
    void onGossipFinish();

    /**
     * 设置发送完成通知器
     *
     * @param notifier 通知器
     */
    void setNotifier(GossipFinishNotifier notifier);
}

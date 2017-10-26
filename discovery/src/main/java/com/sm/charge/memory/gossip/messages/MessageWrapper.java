package com.sm.charge.memory.gossip.messages;


import com.sm.charge.memory.gossip.GossipFinishNotifier;

/**
 * 在集群中进行传播的消息
 *
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:25
 */
public interface MessageWrapper {

    GossipMessage message();

    /**
     * 是否使消息message失效
     *
     * 当同一个节点的同一类型的GossipMessage，后入队的消息会使之前的消息失效
     *
     * @param wrapper GossipMessage
     * @return 如果使message失效，则返回true，否则返回false
     */
    void invalidate(MessageWrapper wrapper);

    boolean invalid();

    /**
     * 获取尝试传递的次数
     */
    int transmits();

    void increaseTransmits();

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

package com.sm.charge.memory.gossip;


import com.sm.charge.memory.gossip.messages.MessageWrapper;

/**
 * 当{@link MessageWrapper}发送完成时，如果存在{@link GossipFinishNotifier},会调用它
 *
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:26
 */
public interface GossipFinishNotifier {
    /**
     * 发送通知
     */
    void finishNotify();
}

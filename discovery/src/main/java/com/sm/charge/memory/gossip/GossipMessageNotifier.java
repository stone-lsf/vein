package com.sm.charge.memory.gossip;


import com.sm.charge.memory.gossip.messages.GossipContent;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 上午12:02
 */
public interface GossipMessageNotifier {

    /**
     * 通知收到某个消息
     *
     * @param message 消息
     */
    void notify(GossipContent message);
}

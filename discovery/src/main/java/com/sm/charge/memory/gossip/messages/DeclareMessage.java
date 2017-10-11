package com.sm.charge.memory.gossip.messages;


import com.sm.charge.memory.gossip.GossipFinishNotifier;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:25
 */
public abstract class DeclareMessage implements GossipMessage {

    private GossipFinishNotifier finishNotifier;


    @Override
    public boolean invalidate(GossipMessage message) {
        return false;
    }

    @Override
    public void onGossipFinish() {
        if (finishNotifier != null) {
            finishNotifier.finishNotify();
        }
    }

    @Override
    public void setNotifier(GossipFinishNotifier notifier) {
        this.finishNotifier = notifier;
    }
}

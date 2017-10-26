package com.sm.charge.memory.gossip.messages;

import com.sm.charge.memory.gossip.GossipFinishNotifier;

/**
 * @author shifeng.luo
 * @version created on 2017/10/27 上午12:01
 */
public class MemberWrapper implements MessageWrapper {
    private GossipFinishNotifier finishNotifier;

    private final GossipMessage message;

    private volatile boolean invalid = false;

    private volatile int transmits;

    public MemberWrapper(GossipMessage message) {
        this.message = message;
    }

    @Override
    public GossipMessage message() {
        return message;
    }

    @Override
    public void invalidate(MessageWrapper wrapper) {
        if (!(wrapper instanceof MemberWrapper)) {
            return;
        }

        MemberWrapper msg = (MemberWrapper) wrapper;
        if (this.message.getNodeId().equals(msg.message.getNodeId())) {
            msg.onGossipFinish();
            msg.invalid = true;
        }
    }

    @Override
    public boolean invalid() {
        return invalid;
    }

    @Override
    public int transmits() {
        return transmits;
    }

    @Override
    public void increaseTransmits() {
        transmits++;
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

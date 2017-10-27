package com.sm.charge.memory.gossip.messages;

import com.sm.charge.memory.gossip.GossipFinishNotifier;

/**
 * @author shifeng.luo
 * @version created on 2017/10/27 上午12:01
 */
public class MemberMessage implements GossipMessage {
    private GossipFinishNotifier finishNotifier;

    private final GossipContent content;

    private volatile boolean invalid = false;

    private volatile int transmits;

    public MemberMessage(GossipContent content) {
        this.content = content;
    }

    @Override
    public GossipContent getContent() {
        return content;
    }

    @Override
    public void invalidate(GossipMessage message) {
        if (!(message instanceof MemberMessage)) {
            return;
        }

        MemberMessage msg = (MemberMessage) message;
        if (this.content.getNodeId().equals(msg.content.getNodeId())) {
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

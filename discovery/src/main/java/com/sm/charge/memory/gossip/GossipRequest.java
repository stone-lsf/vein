package com.sm.charge.memory.gossip;


import com.sm.charge.memory.gossip.messages.GossipMessage;
import com.sm.charge.memory.gossip.messages.MessageWrapper;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午11:22
 */
public class GossipRequest {
    private List<GossipMessage> messages;

    public GossipRequest() {
    }

    public GossipRequest(List<GossipMessage> messages) {
        this.messages = messages;
    }

    public List<GossipMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<GossipMessage> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "GossipRequest{" +
            "messages=" + messages +
            '}';
    }
}

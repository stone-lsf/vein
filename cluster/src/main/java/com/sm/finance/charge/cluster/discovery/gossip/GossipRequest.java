package com.sm.finance.charge.cluster.discovery.gossip;

import com.sm.finance.charge.cluster.discovery.gossip.messages.GossipMessage;

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

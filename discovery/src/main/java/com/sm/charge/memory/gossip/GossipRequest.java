package com.sm.charge.memory.gossip;


import com.sm.charge.memory.gossip.messages.GossipContent;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午11:22
 */
public class GossipRequest {
    private List<GossipContent> messages;

    public GossipRequest() {
    }

    public GossipRequest(List<GossipContent> messages) {
        this.messages = messages;
    }

    public List<GossipContent> getMessages() {
        return messages;
    }

    public void setMessages(List<GossipContent> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "GossipRequest{" +
            "messages=" + messages +
            '}';
    }
}

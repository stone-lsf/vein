package com.vein.discovery.gossip;


import com.vein.discovery.gossip.messages.GossipContent;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午11:22
 */
public class GossipRequest {
    private List<GossipContent> contents;

    public GossipRequest() {
    }

    public GossipRequest(List<GossipContent> contents) {
        this.contents = contents;
    }

    public List<GossipContent> getContents() {
        return contents;
    }

    public void setContents(List<GossipContent> contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "GossipRequest{" +
            "messages=" + contents +
            '}';
    }
}

package com.vein.discovery.pushpull;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:26
 */
public class PushPullRequest {
    private String from;

    private List<PushNodeState> states;

    public PushPullRequest() {
    }

    public PushPullRequest(String from, List<PushNodeState> states) {
        this.from = from;
        this.states = states;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<PushNodeState> getStates() {
        return states;
    }

    public void setStates(List<PushNodeState> states) {
        this.states = states;
    }

    @Override
    public String toString() {
        return "PushPullRequest{" +
            "from='" + from + '\'' +
            ", states=" + states +
            '}';
    }
}

package com.sm.charge.memory.pushpull;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:33
 */
public class PushPullResponse {

    private List<PushNodeState> states;

    public PushPullResponse() {
    }

    public PushPullResponse(List<PushNodeState> states) {
        this.states = states;
    }

    public List<PushNodeState> getStates() {
        return states;
    }

    public void setStates(List<PushNodeState> states) {
        this.states = states;
    }
}

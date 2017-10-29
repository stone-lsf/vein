package com.sm.charge.cluster.messages;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午2:20
 */
public class PullResponse {

    private List<PullState> states;

    public PullResponse() {
    }

    public PullResponse(List<PullState> states) {
        this.states = states;
    }

    public List<PullState> getStates() {
        return states;
    }

    public void setStates(List<PullState> states) {
        this.states = states;
    }

}

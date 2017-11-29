package com.vein.cluster.messages;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午2:20
 */
public class StatePullResponse {

    private List<PullState> states;

    public StatePullResponse() {
    }

    public StatePullResponse(List<PullState> states) {
        this.states = states;
    }

    public List<PullState> getStates() {
        return states;
    }

    public void setStates(List<PullState> states) {
        this.states = states;
    }

}

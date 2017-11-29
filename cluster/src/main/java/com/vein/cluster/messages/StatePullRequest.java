package com.vein.cluster.messages;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午2:19
 */
public class StatePullRequest {
    private PullState state;

    public StatePullRequest() {
    }

    public StatePullRequest(PullState state) {
        this.state = state;
    }

    public PullState getState() {
        return state;
    }

    public void setState(PullState state) {
        this.state = state;
    }
}

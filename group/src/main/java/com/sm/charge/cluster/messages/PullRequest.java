package com.sm.charge.cluster.messages;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午2:19
 */
public class PullRequest {
    private PullState state;

    public PullRequest() {
    }

    public PullRequest(PullState state) {
        this.state = state;
    }

    public PullState getState() {
        return state;
    }

    public void setState(PullState state) {
        this.state = state;
    }
}

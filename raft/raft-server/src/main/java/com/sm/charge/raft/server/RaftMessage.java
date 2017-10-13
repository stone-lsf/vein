package com.sm.charge.raft.server;

import com.sm.charge.raft.server.state.Event;

/**
 * @author shifeng.luo
 * @version created on 2017/10/13 下午4:13
 */
public class RaftMessage implements Event {
    /**
     * 任期
     */
    private long term;

    /**
     * 消息来源
     */
    private long source;

    /**
     * 消息目的地
     */
    private long destination;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public long getDestination() {
        return destination;
    }

    public void setDestination(long destination) {
        this.destination = destination;
    }
}

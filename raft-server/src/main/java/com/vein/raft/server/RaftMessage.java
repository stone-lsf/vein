package com.vein.raft.server;

import com.vein.raft.client.Event;

/**
 * @author shifeng.luo
 * @version created on 2017/10/13 下午4:13
 */
public class RaftMessage implements Event {
    /**
     * 任期
     */
    protected long term;

    /**
     * 消息来源
     */
    protected String source;

    /**
     * 消息目的地
     */
    protected String destination;

    public RaftMessage() {
    }

    public RaftMessage(long term, String source, String destination) {
        this.term = term;
        this.source = source;
        this.destination = destination;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "RaftMessage{" +
            "term=" + term +
            ", source='" + source + '\'' +
            ", destination='" + destination + '\'' +
            '}';
    }
}

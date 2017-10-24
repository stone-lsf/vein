package com.sm.charge.memory.probe;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:08
 */
public class Ping {

    /**
     * ping消息来源节点
     */
    private String from;

    private String to;

    public Ping() {
    }

    public Ping(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "Ping{" +
            "from='" + from + '\'' +
            ", to='" + to + '\'' +
            '}';
    }
}

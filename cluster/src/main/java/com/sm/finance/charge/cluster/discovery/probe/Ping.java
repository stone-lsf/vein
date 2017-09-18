package com.sm.finance.charge.cluster.discovery.probe;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:08
 */
public class Ping {

    /**
     * ping消息来源节点
     */
    private String from;

    public Ping() {
    }

    public Ping(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Ping{" +
            "from='" + from + '\'' +
            '}';
    }
}

package com.sm.charge.discovery.probe;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:08
 */
public class RedirectPing {
    /**
     * 来源节点
     */
    private String from;

    /**
     * 目标节点
     */
    private String target;

    public RedirectPing() {
    }

    public RedirectPing(String from, String target) {
        this.from = from;
        this.target = target;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "RedirectPing{" +
            "from='" + from + '\'' +
            ", target='" + target + '\'' +
            '}';
    }
}

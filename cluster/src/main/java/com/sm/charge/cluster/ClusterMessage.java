package com.sm.charge.cluster;

/**
 * @author shifeng.luo
 * @version created on 2017/11/3 下午5:16
 */
public abstract class ClusterMessage {

    private String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}

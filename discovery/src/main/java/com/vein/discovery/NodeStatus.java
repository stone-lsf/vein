package com.vein.discovery;

/**
 * @author shifeng.luo
 * @version created on 2017/10/25 下午1:00
 */
public enum NodeStatus {
    ALIVE((byte) 1, "alive"),
    SUSPECT((byte) 2, "suspect"),
    DEAD((byte) 3, "dead");

    public final byte code;
    public final String desc;

    NodeStatus(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NodeStatus valueOf(byte code) {
        NodeStatus[] statuses = NodeStatus.values();
        for (NodeStatus status : statuses) {
            if (status.code == code) {
                return status;
            }
        }

        throw new RuntimeException("unknown Status code:" + code);
    }
}

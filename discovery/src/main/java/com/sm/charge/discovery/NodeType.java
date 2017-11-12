package com.sm.charge.discovery;

/**
 * @author shifeng.luo
 * @version created on 2017/10/25 下午1:00
 */
public enum NodeType {
    DATA(1, "数据节点"),
    CANDIDATE(2, "候选节点");

    public final int code;

    public final String desc;

    NodeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NodeType valueOf(byte code) {
        NodeType[] types = NodeType.values();
        for (NodeType type : types) {
            if (type.code == code) {
                return type;
            }
        }

        throw new RuntimeException("unknown Type code:" + code);
    }
}

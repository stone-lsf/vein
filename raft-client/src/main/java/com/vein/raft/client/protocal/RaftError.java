package com.vein.raft.client.protocal;

/**
 * @author shifeng.luo
 * @version created on 2017/11/7 下午10:49
 */
public enum RaftError {
    NO_LEADER_ERROR(1),
    INTERNAL_ERROR(2);

    RaftError(int code) {
        this.code = (byte) code;
    }

    public final byte code;
}

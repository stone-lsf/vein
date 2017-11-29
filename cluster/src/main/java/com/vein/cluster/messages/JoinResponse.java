package com.vein.cluster.messages;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午8:50
 */
public class JoinResponse {
    public static final int SUCCESS = 0;
    public static final int NOT_LEADER = 1;
    public static final int INNER_ERROR = 2;

    private int status;
    private long version;

    public JoinResponse() {
    }

    public JoinResponse(int status) {
        this.status = status;
    }

    public JoinResponse(int status, long version) {
        this.status = status;
        this.version = version;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}

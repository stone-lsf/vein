package com.sm.finance.charge.cluster.replicate;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午5:05
 */
public class ReplicateResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 当success=true时，忽略，否则为最后一条日志的下一个index
     */
    private long nextIndex;

    /**
     * 当前版本号
     */
    private long version;

    public boolean isSuccess() {
        return success;
    }

    public ReplicateResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public long getNextIndex() {
        return nextIndex;
    }

    public ReplicateResponse setNextIndex(long nextIndex) {
        this.nextIndex = nextIndex;
        return this;
    }

    public long getVersion() {
        return version;
    }

    public ReplicateResponse setVersion(long version) {
        this.version = version;
        return this;
    }
}

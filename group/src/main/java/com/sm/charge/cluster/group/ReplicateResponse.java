package com.sm.charge.cluster.group;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午11:47
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(long nextIndex) {
        this.nextIndex = nextIndex;
    }
}

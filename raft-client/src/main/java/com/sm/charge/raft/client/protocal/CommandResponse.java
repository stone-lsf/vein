package com.sm.charge.raft.client.protocal;

/**
 * @author shifeng.luo
 * @version created on 2017/11/7 下午10:47
 */
public class CommandResponse extends RaftResponse {

    private long index;

    private Object result;

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}

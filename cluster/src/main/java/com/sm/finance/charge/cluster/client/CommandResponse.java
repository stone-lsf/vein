package com.sm.finance.charge.cluster.client;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:58
 */
public class CommandResponse {

    private long sequence;

    private Object result;

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}

package com.sm.finance.charge.cluster.replicate;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:52
 */
public interface ArbitrateListener {

    void onSuccess();

    void onFail();
}

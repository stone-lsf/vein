package com.sm.finance.charge.cluster.replicate;

/**
 * 日志复制仲裁器
 *
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:51
 */
public interface ReplicateArbitrator {

    /**
     * 标记复制给某个节点失败
     */
    void flagOneFail();

    /**
     * 标记复制给某个节点成功
     */
    void flagOneSuccess();

    /**
     * 设置仲裁结果监听器
     *
     * @param listener 监听器
     */
    void add(ArbitrateListener listener);

    void start();

}

package com.sm.finance.charge.server.discovery;

/**
 * @author shifeng.luo
 * @version created on 2017/9/2 下午1:46
 *
 *          具有生命周期的组件接口
 */
public interface LifecycleComponent extends LifecycleListener {

    LifeState state();

    void add(LifecycleListener listener);

    void remove(LifecycleListener listener);
}

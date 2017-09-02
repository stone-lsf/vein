package com.sm.finance.charge.server.discovery;

/**
 * @author shifeng.luo
 * @version created on 2017/9/2 下午1:43
 */
public interface LifecycleListener {

    void onStarting();

    void onStarted();

    void onStopping();

    void onStopped();

    void onClosing();

    void onClosed();
}

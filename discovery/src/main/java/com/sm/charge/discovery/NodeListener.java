package com.sm.charge.discovery;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 下午11:03
 */
public interface NodeListener {

    void onJoin(Node node);

    void onUpdate(Node node);

    void onLeave(Node node);
}

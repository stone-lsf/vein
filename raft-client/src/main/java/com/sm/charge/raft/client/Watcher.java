package com.sm.charge.raft.client;

/**
 * @author shifeng.luo
 * @version created on 2017/11/7 下午10:33
 */
public interface Watcher {

    void onEvent(Event event);
}

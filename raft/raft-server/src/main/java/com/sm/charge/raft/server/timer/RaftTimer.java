package com.sm.charge.raft.server.timer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午3:35
 */
public interface RaftTimer extends Runnable{

    int interval();

    void reset();

    void start();

    void stop();
}

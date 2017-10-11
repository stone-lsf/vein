package com.sm.charge.raft.client;


/**
 * @author shifeng.luo
 * @version created on 2017/5/11 下午10:46
 */
public interface Command<T extends Command<T>> {
    byte application = 1;
    byte reconfigure = 2;

    byte type();


}

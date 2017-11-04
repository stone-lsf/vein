package com.sm.charge.raft.client;

/**
 * @author shifeng.luo
 * @version created on 2017/10/18 上午12:54
 */
public class ConfigureCommand implements Command<ConfigureCommand> {
    @Override
    public byte type() {
        return 0;
    }
}

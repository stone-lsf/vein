package com.sm.charge.raft.server.storage.logs;

import com.sm.charge.raft.client.Command;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午3:44
 */
public class TestCommand implements Command {

    private String command;

    public TestCommand() {
    }

    public TestCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}

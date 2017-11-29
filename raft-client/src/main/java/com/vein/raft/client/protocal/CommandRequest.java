package com.vein.raft.client.protocal;

import com.vein.raft.client.Command;

/**
 * @author shifeng.luo
 * @version created on 2017/11/7 下午10:46
 */
public class CommandRequest {

    private Command command;

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}

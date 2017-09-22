package com.sm.finance.charge.cluster.client;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:49
 */
public class CommandRequest {

    private long sequence;

    private Command command;

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}

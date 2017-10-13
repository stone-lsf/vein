package com.sm.charge.raft.server.storage;


import com.sm.charge.raft.client.Command;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:24
 */
public class LogEntry {

    private long index;
    private long term;
    private Command command;

    private int size;

    public LogEntry(Command command, long term) {
        this.command = command;
        this.term = term;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

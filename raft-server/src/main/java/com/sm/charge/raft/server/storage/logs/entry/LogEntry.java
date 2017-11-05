package com.sm.charge.raft.server.storage.logs.entry;


import com.sm.charge.raft.client.Command;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:24
 */
public class LogEntry {
    public static final int INDEX_TERM_LENGTH = 8 + 8;
    /**
     * entry除了数据之外的大小
     * INDEX_TERM_LENGTH + size(4)+checksum(8)
     */
    public static final int ENTRY_OTHER_SIZE = 4 + INDEX_TERM_LENGTH + 8;

    private long index;
    private long term;
    private int size;
    private Command command;

    public LogEntry(Command command, long term) {
        this.command = command;
        this.term = term;
    }

    public LogEntry(long index, long term, int size, Command command) {
        this.index = index;
        this.term = term;
        this.size = size;
        this.command = command;
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

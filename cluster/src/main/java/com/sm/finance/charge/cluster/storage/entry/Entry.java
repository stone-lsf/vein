package com.sm.finance.charge.cluster.storage.entry;

import com.sm.finance.charge.cluster.client.Command;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午3:46
 */
public class Entry {

    private long index;
    private long version;
    private Command command;

    private int size = -1;

    public Entry(Command command) {
        this.command = command;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

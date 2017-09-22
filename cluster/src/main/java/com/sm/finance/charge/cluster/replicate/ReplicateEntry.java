package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.cluster.client.Command;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午7:41
 */
public class ReplicateEntry {

    /**
     * 集群版本号
     */
    private long version;

    /**
     * 数据索引号
     */
    private long index;

    /**
     * 数据
     */
    private Command command;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}

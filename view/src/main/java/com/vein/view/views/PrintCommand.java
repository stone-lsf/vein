package com.vein.view.views;

import com.vein.raft.client.Command;

/**
 * @author shifeng.luo
 * @version created on 2017/12/10 下午3:35
 */
public class PrintCommand implements Command {

    private String print;

    public PrintCommand(String print) {
        this.print = print;
    }

    public String getPrint() {
        return print;
    }

    public void setPrint(String print) {
        this.print = print;
    }
}

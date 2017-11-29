package com.vein.raft.server.events;

import com.vein.raft.server.RaftMember;
import com.vein.raft.server.RaftMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:49
 */
public class LeaveResponse extends RaftMessage {

    public static final int SUCCESS = 0;
    public static final int REDIRECT = 1;
    public static final int RECONFIGURING = 2;
    public static final int NO_LEADER = 3;
    public static final int INTERNAL_ERROR = 4;
    public static final int LOWER_TERM = 5;

    private int status;

    private RaftMember master;

    public boolean isSuccess() {
        return status == SUCCESS;
    }


    public boolean needRedirect() {
        return status == REDIRECT;
    }

    public boolean reconfiguring() {
        return status == RECONFIGURING;
    }

    public boolean remoteError() {
        return status == INTERNAL_ERROR;
    }

    public boolean noLeader() {
        return status == NO_LEADER;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public RaftMember getMaster() {
        return master;
    }

    public void setMaster(RaftMember master) {
        this.master = master;
    }
}

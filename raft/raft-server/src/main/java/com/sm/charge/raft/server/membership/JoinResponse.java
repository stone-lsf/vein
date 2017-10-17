package com.sm.charge.raft.server.membership;

import com.sm.charge.raft.server.RaftMemberContext;
import com.sm.charge.raft.server.RaftMember;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:47
 */
public class JoinResponse {

    public static final int SUCCESS = 0;
    public static final int REDIRECT = 1;
    public static final int RECONFIGURING = 2;


    /**
     * 状态码
     */
    private int status;

    private long term;

    private RaftMemberContext master;

    /**
     * 集群服务器列表
     */
    private List<RaftMember> servers;

    private boolean needInstallSnapshot;


    public boolean isSuccess() {
        return status == SUCCESS;
    }

    public boolean needRedirect() {
        return status == REDIRECT;
    }

    public boolean reconfiguring() {
        return status == RECONFIGURING;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public RaftMemberContext getMaster() {
        return master;
    }

    public void setMaster(RaftMemberContext master) {
        this.master = master;
    }

    public List<RaftMember> getServers() {
        return servers;
    }

    public void setServers(List<RaftMember> servers) {
        this.servers = servers;
    }

    public boolean isNeedInstallSnapshot() {
        return needInstallSnapshot;
    }

    public void setNeedInstallSnapshot(boolean needInstallSnapshot) {
        this.needInstallSnapshot = needInstallSnapshot;
    }
}

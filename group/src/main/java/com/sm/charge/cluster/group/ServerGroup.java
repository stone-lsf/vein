package com.sm.charge.cluster.group;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午4:50
 */
public class ServerGroup {

    private Server leader;
    private List<Server> servers;

    private int quorum;

    public int getQuorum() {
        return quorum;
    }

    public void setQuorum(int quorum) {
        this.quorum = quorum;
    }

    public Server getLeader() {
        return leader;
    }

    public void setLeader(Server leader) {
        this.leader = leader;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }
}

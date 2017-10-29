package com.sm.charge.cluster.group;

import com.sm.finance.charge.common.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午4:50
 */
public class ServerGroup {

    private volatile long version;
    private Server leader;
    private ConcurrentMap<Address, Server> servers = new ConcurrentHashMap<>();

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
        return new ArrayList<>(servers.values());
    }

    public void add(Server server) {
        servers.put(server.getAddress(), server);
    }

    public Server get(Address address) {
        return servers.get(address);
    }


    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}

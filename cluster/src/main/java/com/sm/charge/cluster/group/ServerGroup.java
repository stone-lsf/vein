package com.sm.charge.cluster.group;

import com.sm.charge.cluster.Server;
import com.sm.finance.charge.common.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午4:50
 */
public class ServerGroup {

    private final String name;
    private Server leader;
    private int quorum;

    private ConcurrentMap<Address, Server> servers = new ConcurrentHashMap<>();
    private final Map<Long, CompletableFuture<Boolean>> messageFutures = new ConcurrentHashMap<>();

    public ServerGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

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

    public CompletableFuture<Boolean> getMessageFuture(long index) {
        return messageFutures.get(index);
    }

    public void addMessageFuture(long index, CompletableFuture<Boolean> future) {
        messageFutures.put(index, future);
    }

    public void clearMessageFutures() {
        messageFutures.clear();
    }
}

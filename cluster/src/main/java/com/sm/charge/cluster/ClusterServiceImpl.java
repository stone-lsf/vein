package com.sm.charge.cluster;

import com.google.common.base.Preconditions;

import com.sm.charge.cluster.exceptions.NoGroupException;
import com.sm.charge.cluster.group.GroupService;
import com.sm.charge.cluster.group.GroupServiceImpl;
import com.sm.charge.cluster.group.ServerGroup;
import com.sm.charge.memory.DiscoveryConfig;
import com.sm.charge.memory.DiscoveryService;
import com.sm.charge.memory.DiscoveryServiceImpl;
import com.sm.charge.raft.server.LogStateMachine;
import com.sm.charge.raft.server.RaftConfig;
import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.RaftServerImpl;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.SystemConstants;
import com.sm.finance.charge.common.base.Configure;
import com.sm.finance.charge.common.base.ConfigureLoader;
import com.sm.finance.charge.common.utils.AddressUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午4:46
 */
public class ClusterServiceImpl extends AbstractService implements ClusterService {

    private final Server self;
    private final ClusterConfig config;
    private final LogStateMachine stateMachine;
    private DiscoveryService discoveryService;
    private RaftServer raftServer;
    private Store store;
    private ConcurrentMap<String, GroupService> groupServices = new ConcurrentHashMap<>();


    public ClusterServiceImpl(ClusterConfig config, LogStateMachine stateMachine) {
        this.stateMachine = stateMachine;
        Address address = AddressUtil.getLocalAddress(config.getBindPort());
        String serverId = config.getServerId(address);
        this.self = new Server(serverId, address);
        this.config = config;
    }


    @Override
    protected void doStart() throws Exception {
        String profile = SystemConstants.PROFILE == null ? "dev" : SystemConstants.PROFILE;

        Configure configure = ConfigureLoader.loader(profile + File.pathSeparator + "discovery.properties");
        discoveryService = new DiscoveryServiceImpl(new DiscoveryConfig(configure));
        boolean success = discoveryService.join();
        if (!success) {
            throw new RuntimeException("discovery service join failed");
        }

        ServerType type = config.getServerType();
        if (type != ServerType.core) {
            return;
        }
        Preconditions.checkState(stateMachine != null);

        configure = ConfigureLoader.loader(profile + File.pathSeparator + "raft.properties");
        RaftConfig raftConfig = new RaftConfig(configure);
        raftServer = new RaftServerImpl(raftConfig, stateMachine);
        raftServer.join();
    }

    @Override
    protected void doClose() throws Exception {
        if (raftServer != null) {
            raftServer.close();
        }
        discoveryService.close();
    }

    @Override
    public void createGroup(String groupName, List<ServerInfo> servers) {
        ServerGroup group = new ServerGroup(groupName);

        for (ServerInfo info : servers) {
            Server server = new Server(info.getServerId(), info.getAddress());
            group.add(server);
        }

        GroupService groupService = new GroupServiceImpl(group, self, config, store);
        groupServices.put(groupName, groupService);
    }

    @Override
    public CompletableFuture<Boolean> receive(ClusterMessage message) {
        String group = message.getGroup();
        GroupService groupService = groupServices.get(group);
        if (groupService == null) {
            logger.error("receive group:{} message,but don't contain that");
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(new NoGroupException(group + " don't exist!"));
            return future;
        }

        return groupService.receive(message);
    }
}

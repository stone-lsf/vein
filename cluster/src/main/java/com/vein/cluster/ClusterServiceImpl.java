package com.vein.cluster;

import com.google.common.base.Preconditions;

import com.vein.cluster.exceptions.NoGroupException;
import com.vein.cluster.group.GroupService;
import com.vein.cluster.group.GroupServiceImpl;
import com.vein.cluster.group.ServerGroup;
import com.vein.discovery.DiscoveryConfig;
import com.vein.discovery.DiscoveryServer;
import com.vein.discovery.DiscoveryServerImpl;
import com.vein.raft.server.LogStateMachine;
import com.vein.raft.server.RaftConfig;
import com.vein.raft.server.RaftServer;
import com.vein.raft.server.RaftServerImpl;
import com.vein.common.AbstractService;
import com.vein.common.Address;
import com.vein.common.SystemConstants;
import com.vein.common.base.Configure;
import com.vein.common.base.ConfigureLoader;
import com.vein.common.utils.AddressUtil;
import com.vein.transport.api.Connection;
import com.vein.transport.api.Transport;
import com.vein.transport.api.TransportFactory;
import com.vein.transport.api.TransportServer;
import com.vein.transport.api.exceptions.BindException;

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
    private TransportServer transportServer;
    private DiscoveryServer discoveryService;
    private RaftServer raftServer;
    private Store store;
    private ConcurrentMap<String, GroupService> groupServices = new ConcurrentHashMap<>();


    public ClusterServiceImpl(ClusterConfig config, LogStateMachine stateMachine) {
        this.stateMachine = stateMachine;
        Address address = AddressUtil.getLocalAddress(config.getBindPort());
        String serverId = config.getServerId(address);
        this.self = new Server(serverId, address, config.getServerType());
        this.config = config;
    }


    @Override
    protected void doStart() throws Exception {
        String profile = SystemConstants.PROFILE == null ? "dev" : SystemConstants.PROFILE;

        Configure configure = ConfigureLoader.loader(profile + File.separator + "discovery.properties");
        discoveryService = new DiscoveryServerImpl(new DiscoveryConfig(configure));
        discoveryService.start();
        discoveryService.join();

        ServerType type = self.getType();
        if (type != ServerType.core) {
            return;
        }
        Preconditions.checkState(stateMachine != null);

        configure = ConfigureLoader.loader(profile + File.separator + "raft.properties");
        RaftConfig raftConfig = new RaftConfig(configure);
        raftServer = new RaftServerImpl(raftConfig, stateMachine);
        raftServer.start();
        raftServer.join();

        listenPort(config.getBindPort());
    }


    private void listenPort(int port) {
        Transport transport = TransportFactory.create(config.getTransportType());
        this.transportServer = transport.server();
        try {
            transportServer.listen(port, (Connection connection) -> logger.info("accept connection:{}", connection.getConnectionId()));
        } catch (BindException e) {
            logger.error("bind port:{} caught exception", port, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doClose() throws Exception {
        if (raftServer != null) {
            raftServer.close();
        }
        discoveryService.close();

        transportServer.close();
    }

    @Override
    public void createGroup(String groupName, List<ServerInfo> servers) {
        ServerGroup group = new ServerGroup(groupName);

        for (ServerInfo info : servers) {
            Server server = new Server(info.getServerId(), info.getAddress(), info.getType());
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

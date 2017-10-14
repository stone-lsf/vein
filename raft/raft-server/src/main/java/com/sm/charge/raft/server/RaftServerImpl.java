package com.sm.charge.raft.server;

import com.sm.charge.memory.DiscoveryService;
import com.sm.charge.raft.client.Command;
import com.sm.charge.raft.server.election.MasterListener;
import com.sm.charge.raft.server.membership.JoinRequest;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.membership.LeaveRequest;
import com.sm.charge.raft.server.membership.LeaveResponse;
import com.sm.charge.raft.server.state.CandidateState;
import com.sm.charge.raft.server.state.FollowerState;
import com.sm.charge.raft.server.state.LeaderState;
import com.sm.charge.raft.server.state.PassiveState;
import com.sm.charge.raft.server.state.ServerState;
import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.charge.raft.server.storage.MemberStateManager;
import com.sm.charge.raft.server.storage.Snapshot;
import com.sm.charge.raft.server.storage.SnapshotManager;
import com.sm.charge.raft.server.storage.file.FileLog;
import com.sm.charge.raft.server.storage.file.FileMemberStateManager;
import com.sm.charge.raft.server.storage.file.FileSnapshotManager;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.AddressUtil;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportFactory;
import com.sm.finance.charge.transport.api.TransportServer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:14
 */
public class RaftServerImpl extends AbstractService implements RaftServer, RaftListener, MasterListener {

    private final DiscoveryService discoveryService;
    private final RaftConfig raftConfig;
    private final RaftCluster cluster;
    private final Map<RaftState, ServerState> serverStates = new HashMap<>();
    private final RaftMember self;
    private final ServerStateMachine serverStateMachine;
    private final TransportServer transportServer;
    private final TransportClient client;
    private final Log log;
    private final LogStateMachine logStateMachine;
    private final SnapshotManager snapshotManager;
    private final MemberStateManager memberStateManager;

    private volatile RaftState state;


    public RaftServerImpl(DiscoveryService discoveryService, RaftConfig raftConfig, LogStateMachine logStateMachine) {
        this.discoveryService = discoveryService;
        this.logStateMachine = logStateMachine;
        this.raftConfig = raftConfig;

        Transport transport = TransportFactory.create(raftConfig.getTransportType());
        this.transportServer = transport.server();
        this.client = transport.client();
        Address address = AddressUtil.getLocalAddress(raftConfig.getPort());
        this.self = new RaftMember(client, address.ipPort(), address);

        this.cluster = new RaftClusterImpl(raftConfig.getClusterName(), self);
        this.log = new FileLog();
        this.snapshotManager = new FileSnapshotManager(raftConfig.getSnapshotDrectory(), raftConfig.getSnapshotName());
        this.memberStateManager = new FileMemberStateManager();

        ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("LogApplyThread"));
        this.serverStateMachine = new ServerStateMachine(log, self, logStateMachine, snapshotManager, executor);

        ServerContext context = initContext(raftConfig);
        initStates(context);
    }


    private ServerContext initContext(RaftConfig raftConfig) {
        ServerContext.Builder builder = ServerContext.builder();
        builder.setCluster(cluster)
            .setLog(log)
            .setMemberStateManager(memberStateManager)
            .setRaftConfig(raftConfig)
            .setSelf(self)
            .setSnapshotManager(snapshotManager)
            .setStateMachine(serverStateMachine);

        return builder.build();
    }

    private void initStates(ServerContext context) {
        PassiveState catchUpState = new PassiveState(this, context);
        FollowerState followerState = new FollowerState(this, context);
        CandidateState candidateState = new CandidateState(this, context);
        LeaderState leaderState = new LeaderState(this, context);

        serverStates.put(RaftState.PASSIVE, catchUpState);
        serverStates.put(RaftState.FOLLOWER, followerState);
        serverStates.put(RaftState.CANDIDATE, candidateState);
        serverStates.put(RaftState.LEADER, leaderState);
    }

    @Override
    public long getId() {
        return self.getId();
    }

    @Override
    public void transition(RaftState newState) {
        if (newState == state) {
            return;
        }

        ServerState serverState = serverStates.get(state);
        serverState.suspect();
        serverState = serverStates.get(newState);
        serverState.wakeup();
    }

    public CompletableFuture<Boolean> join() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        discoveryService.join(cluster.name());

        return future;
    }

    @Override
    public CompletableFuture<JoinResponse> handle(JoinRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> leave() {
        return null;
    }

    @Override
    public CompletableFuture<LeaveResponse> handle(LeaveRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Object> handle(Command command) {
        LogEntry entry = new LogEntry(command, cluster.version());

        return null;
    }


    @Override
    public void onInstallComplete(Snapshot snapshot) {
        logger.info(" install snapshot:{} completed，current state[{}]", snapshot.index(), state);
        state = state.onInstallComplete(snapshot, this);
    }

    @Override
    public void onElectTimeout() {
        logger.info(" elect timeout，current state[{}]", state);
        state = state.onElectTimeout(this);
    }

    @Override
    public void onElectAsMaster() {
        logger.info("server elect as master，current state[{}]", state);
        state = state.onElectAsMaster(this);
    }

    @Override
    public void onNewLeader() {
        logger.info("server found new master，current state[{}]", state);
        state = state.onNewLeader(this);
    }

    @Override
    public void onFallBehind() {
        logger.info("server found higher term server，current state[{}]", state);
        state = state.onFallBehind(this);
    }

    @Override
    protected void doStart() throws Exception {
        int port = raftConfig.getPort();
        transportServer.listen(port, (Connection connection) -> logger.info("accept connection:{}", connection.getConnectionId()));

        join();
    }

    @Override
    protected void doClose() {

    }

    @Override
    public void onMaster() {
        try {
        } catch (Exception e) {
            logger.error("start replicate service failure", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void offMaster() {
        try {
        } catch (Exception e) {
            logger.error("stop replicate service failure", e);
            throw new RuntimeException(e);
        }
    }
}

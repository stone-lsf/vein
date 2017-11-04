package com.sm.charge.raft.server;

import com.sm.charge.raft.client.Command;
import com.sm.charge.raft.server.election.MasterListener;
import com.sm.charge.raft.server.election.VoteRequest;
import com.sm.charge.raft.server.election.VoteResponse;
import com.sm.charge.raft.server.handlers.AppendRequestHandler;
import com.sm.charge.raft.server.handlers.InstallSnapshotRequestHandler;
import com.sm.charge.raft.server.handlers.JoinRequestHandler;
import com.sm.charge.raft.server.handlers.LeaveRequestHandler;
import com.sm.charge.raft.server.handlers.VoteRequestHandler;
import com.sm.charge.raft.server.membership.InstallSnapshotRequest;
import com.sm.charge.raft.server.membership.InstallSnapshotResponse;
import com.sm.charge.raft.server.membership.JoinRequest;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.membership.LeaveRequest;
import com.sm.charge.raft.server.membership.LeaveResponse;
import com.sm.charge.raft.server.replicate.AppendRequest;
import com.sm.charge.raft.server.replicate.AppendResponse;
import com.sm.charge.raft.server.replicate.Replicator;
import com.sm.charge.raft.server.state.CandidateState;
import com.sm.charge.raft.server.state.EventExecutor;
import com.sm.charge.raft.server.state.FollowerState;
import com.sm.charge.raft.server.state.LeaderState;
import com.sm.charge.raft.server.state.PassiveState;
import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.charge.raft.server.storage.MemberStateManager;
import com.sm.charge.raft.server.storage.SnapshotManager;
import com.sm.charge.raft.server.storage.file.FileLog;
import com.sm.charge.raft.server.storage.file.FileMemberStateManager;
import com.sm.charge.raft.server.storage.file.FileSnapshotManager;
import com.sm.charge.raft.server.timer.ElectTimeoutTimer;
import com.sm.charge.raft.server.timer.HeartbeatTimeoutTimer;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.common.utils.AddressUtil;
import com.sm.finance.charge.common.utils.RandomUtil;
import com.sm.finance.charge.common.utils.ThreadUtil;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportFactory;
import com.sm.finance.charge.transport.api.TransportServer;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static com.sm.charge.raft.server.RaftState.CANDIDATE;
import static com.sm.charge.raft.server.RaftState.FOLLOWER;
import static com.sm.charge.raft.server.RaftState.LEADER;
import static com.sm.charge.raft.server.RaftState.PASSIVE;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:14
 */
public class RaftServerImpl extends AbstractService implements RaftServer, MasterListener {

    private final RaftConfig raftConfig;
    private final RaftCluster cluster;
    private final RaftMember self;
    private final ServerStateMachine serverStateMachine;
    private final TransportServer transportServer;
    private final TransportClient client;
    private final Log log;
    private final LogStateMachine logStateMachine;
    private final SnapshotManager snapshotManager;
    private final MemberStateManager memberStateManager;
    private final ServerContext context;
    private final Replicator replicator;


    public RaftServerImpl(RaftConfig raftConfig, LogStateMachine logStateMachine) {
        this.logStateMachine = logStateMachine;
        this.raftConfig = raftConfig;

        Transport transport = TransportFactory.create(raftConfig.getTransportType());
        this.transportServer = transport.server();
        this.client = transport.client();


        Address address = AddressUtil.getLocalAddress(raftConfig.getPort());
        this.self = new RaftMember(client, address.getAddressStr(), address, null);
        this.cluster = new RaftClusterImpl(raftConfig.getClusterName(), self);
        this.log = new FileLog();
        this.snapshotManager = new FileSnapshotManager(raftConfig.getSnapshotDirectory(), raftConfig.getSnapshotName());
        this.memberStateManager = new FileMemberStateManager();

        ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("LogApplyThread"));
        this.serverStateMachine = new ServerStateMachine(log, self, logStateMachine, snapshotManager, executor);

        this.context = initContext(raftConfig);
        this.replicator = new Replicator(context, raftConfig.getMaxAppendSize(), raftConfig.getHeartbeatInterval());

        initStates();
        registerEventHandler();
    }


    private ServerContext initContext(RaftConfig raftConfig) {
        ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("RaftEventPool"));
        EventExecutor eventExecutor = new RaftEventExecutor(executor);

        ServerContext.Builder builder = ServerContext.builder();
        builder.setCluster(cluster)
            .setLog(log)
            .setMemberStateManager(memberStateManager)
            .setRaftConfig(raftConfig)
            .setSelf(self)
            .setSnapshotManager(snapshotManager)
            .setStateMachine(serverStateMachine)
            .setEventExecutor(eventExecutor);

        return builder.build();
    }

    private void initStates() {
        PassiveState catchUpState = new PassiveState(context);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RaftTimerPool"));
        HeartbeatTimeoutTimer heartbeatTimeoutTimer = new HeartbeatTimeoutTimer(raftConfig.getHeartbeatTimeout(), executor, context);
        FollowerState followerState = new FollowerState(context, heartbeatTimeoutTimer);

        ElectTimeoutTimer electTimeoutTimer = new ElectTimeoutTimer(executor, raftConfig.getMaxElectTimeout(), raftConfig.getMinElectTimeout(), context);
        CandidateState candidateState = new CandidateState(context, electTimeoutTimer);

        LeaderState leaderState = new LeaderState(context, replicator);

        context.add(PASSIVE, catchUpState);
        context.add(FOLLOWER, followerState);
        context.add(CANDIDATE, candidateState);
        context.add(LEADER, leaderState);
    }

    private void registerEventHandler() {
        EventExecutor executor = context.getEventExecutor();
        executor.register(VoteRequest.class, request -> {
            return context.getServerState().handle(request);
        });

        executor.register(VoteResponse.class, response -> {
            context.getServerState().handle(response);
        });

        executor.register(AppendRequest.class, request -> {
            return context.getServerState().handle(request);
        });

        executor.register(AppendResponse.class, response -> {
            context.getServerState().handle(response);
        });

        executor.register(InstallSnapshotRequest.class, request -> {
            return context.getServerState().handle(request);
        });

        executor.register(InstallSnapshotResponse.class, response -> {
            context.getServerState().handle(response);
        });

    }

    @Override
    public String getId() {
        return self.getNodeId();
    }

    public boolean join() {
        String membersStr = raftConfig.getMembers();
        List<Address> members = AddressUtil.parseList(membersStr);

        Set<RaftMember> raftMembers = members.stream()
            .filter(m -> !m.equals(self.getAddress()))
            .map(m -> new RaftMember(client, m.getAddressStr(), m, replicator))
            .collect(Collectors.toSet());

        raftMembers.forEach(cluster::add);

        if (raftMembers.isEmpty()) {
            logger.error("don't have members");
            throw new RuntimeException("don't config members");
        }

        joinTimes = 0;
        return join(new ArrayList<>(raftMembers));
    }

    private volatile int joinTimes = 0;

    private boolean join(List<RaftMember> members) {
        if (joinTimes > raftConfig.getJoinRetryTimes()) {
            throw new RuntimeException("join cluster failure,retry times:" + joinTimes);
        }
        joinTimes++;
        int index = RandomUtil.random(members.size());
        RaftMember member = members.get(index);

        while (self.getNodeId().equals(member.getNodeId())) {
            index = RandomUtil.random(members.size());
            member = members.get(index);
        }

        Connection connection = member.getState().getConnection();
        if (connection != null) {
            if (doJoin(connection)) {
                return true;
            }
            ThreadUtil.sleepUnInterrupted(20000);
        }

        return join(members);
    }

    private boolean doJoin(Connection connection) {
        JoinRequest request = new JoinRequest(self.getNodeId(), self.getAddress());
        try {
            JoinResponse response = connection.syncRequest(request);
            context.getServerState().handle(response);
            if (response.getTerm() < self.getTerm()) {
                return false;
            }

            if (response.isSuccess()) {
                context.getServerState().wakeup();
                return true;
            }

            if (response.needRedirect()) {
                String masterId = response.getMaster().getNodeId();
                if (masterId.equals(self.getNodeId())) {
                    return true;
                }
                System.out.println("master id:"+masterId);
                RaftMember master = cluster.member(masterId);
                connection = master.getState().getConnection();
                return connection != null && doJoin(connection);
            }

            if (response.reconfiguring()) {
                ThreadUtil.sleepUnInterrupted(2000);
                return doJoin(connection);
            }

            return false;
        } catch (Exception e) {
            logger.error("{}: join to member:{} caught exception", self.getNodeId(), connection.remoteAddress(), e);
            return false;
        }
    }

    @Override
    public CompletableFuture<JoinResponse> handle(JoinRequest request, RequestContext requestContext) {
        return CompletableFuture.supplyAsync(() -> context.getServerState().handle(request, requestContext));
    }

    @Override
    public CompletableFuture<Boolean> leave() {
        return null;
    }

    @Override
    public CompletableFuture<LeaveResponse> handle(LeaveRequest request, RequestContext requestContext) {
        return CompletableFuture.supplyAsync(() -> context.getServerState().handle(request, requestContext));
    }

    @Override
    public CompletableFuture<VoteResponse> handle(VoteRequest request) {
        return context.getEventExecutor().submit(request);
    }

    @Override
    public CompletableFuture<AppendResponse> handle(AppendRequest request) {
        return context.getEventExecutor().submit(request);
    }

    @Override
    public CompletableFuture<InstallSnapshotResponse> handle(InstallSnapshotRequest request) {
        return context.getEventExecutor().submit(request);
    }

    @Override
    public CompletableFuture<Object> handle(Command command) {
        LogEntry entry = new LogEntry(command, cluster.version());

        return null;
    }


    @Override
    protected void doStart() throws Exception {
        int port = raftConfig.getPort();
        transportServer.listen(port, (Connection connection) -> logger.info("accept connection:{}", connection.getConnectionId()));

        ConnectionManager manager = transportServer.getConnectionManager();
        manager.registerMessageHandler(new JoinRequestHandler(this));
        manager.registerMessageHandler(new LeaveRequestHandler(this));
        manager.registerMessageHandler(new VoteRequestHandler(this));
        manager.registerMessageHandler(new AppendRequestHandler(this));
        manager.registerMessageHandler(new InstallSnapshotRequestHandler(this));

        context.getServerState().wakeup();
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

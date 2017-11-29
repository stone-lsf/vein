package com.vein.raft.server;

import com.vein.raft.client.CommandTypes;
import com.vein.raft.client.protocal.CommandRequest;
import com.vein.raft.client.protocal.RaftError;
import com.vein.raft.client.protocal.RaftResponse;
import com.vein.raft.server.events.AppendRequest;
import com.vein.raft.server.events.AppendResponse;
import com.vein.raft.server.events.EventExecutor;
import com.vein.raft.server.events.InstallSnapshotRequest;
import com.vein.raft.server.events.InstallSnapshotResponse;
import com.vein.raft.server.events.JoinRequest;
import com.vein.raft.server.events.JoinResponse;
import com.vein.raft.server.events.LeaveRequest;
import com.vein.raft.server.events.LeaveResponse;
import com.vein.raft.server.events.VoteRequest;
import com.vein.raft.server.events.VoteResponse;
import com.vein.raft.server.handlers.AppendRequestHandler;
import com.vein.raft.server.handlers.InstallSnapshotRequestHandler;
import com.vein.raft.server.handlers.JoinRequestHandler;
import com.vein.raft.server.handlers.LeaveRequestHandler;
import com.vein.raft.server.handlers.VoteRequestHandler;
import com.vein.raft.server.state.CandidateState;
import com.vein.raft.server.state.FollowerState;
import com.vein.raft.server.state.LeaderState;
import com.vein.raft.server.state.PassiveState;
import com.vein.raft.server.state.support.Replicator;
import com.vein.raft.server.state.support.timer.ElectTimeoutTimer;
import com.vein.raft.server.state.support.timer.HeartbeatTimeoutTimer;
import com.vein.raft.server.storage.logs.RaftLogger;
import com.vein.raft.server.storage.logs.entry.LogEntry;
import com.vein.raft.server.storage.snapshot.SnapshotManager;
import com.vein.raft.server.storage.snapshot.file.FileSnapshotManager;
import com.vein.raft.server.storage.state.FileMemberStateManager;
import com.vein.raft.server.storage.state.MemberStateManager;
import com.vein.common.AbstractService;
import com.vein.common.Address;
import com.vein.common.NamedThreadFactory;
import com.vein.common.utils.AddressUtil;
import com.vein.common.utils.FileUtil;
import com.vein.common.utils.RandomUtil;
import com.vein.common.utils.ThreadUtil;
import com.vein.serializer.api.Serializer;
import com.vein.serializer.api.SerializerFactory;
import com.vein.transport.api.Connection;
import com.vein.transport.api.ConnectionManager;
import com.vein.transport.api.Transport;
import com.vein.transport.api.TransportClient;
import com.vein.transport.api.TransportFactory;
import com.vein.transport.api.TransportServer;
import com.vein.transport.api.support.RequestContext;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static com.vein.raft.server.RaftState.CANDIDATE;
import static com.vein.raft.server.RaftState.FOLLOWER;
import static com.vein.raft.server.RaftState.LEADER;
import static com.vein.raft.server.RaftState.PASSIVE;

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
    private final RaftLogger raftLogger;
    private final SnapshotManager snapshotManager;
    private final MemberStateManager memberStateManager;
    private final ServerContext context;
    private final Replicator replicator;


    public RaftServerImpl(RaftConfig raftConfig, LogStateMachine logStateMachine) {
        this.raftConfig = raftConfig;

        Transport transport = TransportFactory.create(raftConfig.getTransportType());
        this.transportServer = transport.server();
        this.client = transport.client();

        Address address = AddressUtil.getLocalAddress(raftConfig.getPort());
        this.self = new RaftMember(client, address.getAddressStr(), address, null);
        this.cluster = new RaftClusterImpl(raftConfig.getClusterName(), self);
        this.raftLogger = initLogger();

        this.snapshotManager = new FileSnapshotManager(raftConfig.getSnapshotDirectory(), raftConfig.getSnapshotName(), logStateMachine.getSerializer());
        this.memberStateManager = new FileMemberStateManager();

        ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("LogApplyThread"));
        this.serverStateMachine = new ServerStateMachine(raftLogger, self, logStateMachine, snapshotManager, executor);

        this.context = initContext(raftConfig);
        this.replicator = new Replicator(context, raftConfig.getMaxAppendSize(), raftConfig.getHeartbeatInterval());

        initStates();
        registerEventHandler();
    }

    private RaftLogger initLogger() {
        String name = raftConfig.getLogName();
        File directory;
        try {
            directory = FileUtil.mkDirIfAbsent(raftConfig.getLogDirectory());
        } catch (NotDirectoryException e) {
            logger.error("path:{} is not a directory", raftConfig.getLogDirectory());
            throw new IllegalStateException(e);
        }

        Serializer serializer = SerializerFactory.create(raftConfig.getSerializeType(), new CommandTypes());
        int logFileMaxSize = raftConfig.getLogFileMaxSize();
        int logFileMaxEntries = raftConfig.getLogFileMaxEntries();
        int commandMaxSize = raftConfig.getCommandMaxSize();

        return new RaftLogger(name, directory, serializer, logFileMaxSize, commandMaxSize, logFileMaxEntries);
    }


    private ServerContext initContext(RaftConfig raftConfig) {
        ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("RaftEventPool"));
        EventExecutor eventExecutor = new RaftEventExecutor(executor);

        ServerContext.Builder builder = ServerContext.builder();
        builder.setCluster(cluster)
            .setRaftLogger(raftLogger)
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

    @Override
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
            JoinResponse response = connection.syncRequest(request, raftConfig.getJoinTimeout());
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
                System.out.println("master id:" + masterId);
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
    public <T> CompletableFuture<RaftResponse<T>> handle(CommandRequest request) {
        RaftMember master = cluster.master();
        if (master == null) {
            RaftResponse<T> response = RaftResponse.fail(RaftError.NO_LEADER_ERROR);
            return CompletableFuture.completedFuture(response);
        }


        if (!master.getNodeId().equals(self.getNodeId())) {
            return forward(request, master);
        }

        LogEntry entry = new LogEntry(request.getCommand(), cluster.version());
        long index = raftLogger.append(entry);
        CompletableFuture<T> commitFuture = new CompletableFuture<>();
        self.getState().addCommitFuture(index, commitFuture);

        CompletableFuture<RaftResponse<T>> future = new CompletableFuture<>();
        commitFuture.whenComplete((result, error) -> {
            if (error == null) {
                RaftResponse<T> response = RaftResponse.success(result);
                future.complete(response);
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }


    private <T> CompletableFuture<RaftResponse<T>> forward(CommandRequest request, RaftMember master) {
        CompletableFuture<RaftResponse<T>> future = new CompletableFuture<>();
        Connection connection = master.getState().getConnection();
        if (connection == null) {
            logger.error("leader:{} connection is null", master.getNodeId());
            RaftResponse<T> response = RaftResponse.fail(RaftError.INTERNAL_ERROR);
            return CompletableFuture.completedFuture(response);
        }

        connection.<RaftResponse<T>>request(request).whenComplete((response, error) -> {
            if (error == null) {
                future.complete(response);
            } else {
                logger.error("forward command request:{} to leader:{} caught exception", request, master.getNodeId(), error);
                future.completeExceptionally(error);
            }
        });
        return future;
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

    public static void main(String[] args) {
        CompletableFuture<Integer> futureCount = CompletableFuture.supplyAsync(
            () -> {
                try {
                    // Simulate long running task  模拟长时间运行任务
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                return 10;
            });

        futureCount.whenComplete((result,error)->{
            System.out.println(result);
        });

        ThreadUtil.sleepUnInterrupted(200000);
    }
}

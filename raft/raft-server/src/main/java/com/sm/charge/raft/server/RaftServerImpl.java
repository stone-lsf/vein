package com.sm.charge.raft.server;

import com.sm.charge.raft.client.Command;
import com.sm.charge.raft.server.election.MasterListener;
import com.sm.charge.raft.server.membership.JoinRequest;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.membership.LeaveRequest;
import com.sm.charge.raft.server.membership.LeaveResponse;
import com.sm.charge.raft.server.replicate.Replicator;
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
import com.sm.charge.raft.server.timer.ElectTimeoutTimer;
import com.sm.charge.raft.server.timer.HeartbeatTimeoutTimer;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.utils.AddressUtil;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.common.utils.RandomUtil;
import com.sm.finance.charge.common.utils.ThreadUtil;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportFactory;
import com.sm.finance.charge.transport.api.TransportServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class RaftServerImpl extends AbstractService implements RaftServer, RaftListener, MasterListener {

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
    private final ServerContext context;
    private final Replicator replicator;

    private volatile RaftState state = CANDIDATE;


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
        this.replicator = new Replicator(context, raftConfig.getMaxAppendSize());

        initStates();


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

    private void initStates() {
        PassiveState catchUpState = new PassiveState(this, context);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RaftTimerPool"));
        HeartbeatTimeoutTimer heartbeatTimeoutTimer = new HeartbeatTimeoutTimer(raftConfig.getHeartbeatTimeout(), executor);
        FollowerState followerState = new FollowerState(this, context, heartbeatTimeoutTimer);

        ElectTimeoutTimer electTimeoutTimer = new ElectTimeoutTimer(executor, raftConfig.getMaxElectTimeout(), raftConfig.getMinElectTimeout());
        CandidateState candidateState = new CandidateState(this, context, electTimeoutTimer);

        LeaderState leaderState = new LeaderState(this, context, replicator);

        serverStates.put(PASSIVE, catchUpState);
        serverStates.put(FOLLOWER, followerState);
        serverStates.put(CANDIDATE, candidateState);
        serverStates.put(LEADER, leaderState);
    }

    @Override
    public String getId() {
        return self.getNodeId();
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
            return false;
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

        while (member.getNodeId() == self.getNodeId()) {
            index = RandomUtil.random(members.size());
            member = members.get(index);
        }

        Connection connection = member.getState().getConnection();
        if (connection != null) {
            return doJoin(connection);
        }

        return join(members);
    }

    private boolean doJoin(Connection connection) {
        JoinRequest request = new JoinRequest(self.getNodeId(), self.getAddress());
        try {
            JoinResponse response = connection.syncRequest(request);
            serverStates.get(state).handle(response);
            if (response.getTerm() < self.getTerm()) {
                return false;
            }

            if (response.isSuccess()) {
                serverStates.get(state).wakeup();
                return true;
            }

            if (response.needRedirect()) {
                String masterId = response.getMaster().getNodeId();
                RaftMember master = cluster.member(masterId);
                connection = master.getState().getConnection();
                if (connection != null) {
                    return doJoin(connection);
                }
                return false;
            }

            if (response.reconfiguring()) {
                ThreadUtil.sleep(20000);
                return doJoin(connection);
            }

            return false;
        } catch (Exception e) {
            logger.error("join to member:{} caught exception", connection.remoteAddress(), e);
            return false;
        }
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

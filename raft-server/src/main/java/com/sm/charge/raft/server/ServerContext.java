package com.sm.charge.raft.server;


import com.sm.charge.raft.server.state.EventExecutor;
import com.sm.charge.raft.server.state.ServerState;
import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.MemberStateManager;
import com.sm.charge.raft.server.storage.Snapshot;
import com.sm.charge.raft.server.storage.SnapshotManager;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.TransportClient;

import java.util.HashMap;
import java.util.Map;

import static com.sm.charge.raft.server.RaftState.FOLLOWER;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午11:00
 */
public class ServerContext extends LoggerSupport {

    private final TransportClient client;

    private final RaftConfig raftConfig;

    private final SnapshotManager snapshotManager;

    private final Log log;

    private final RaftMember self;

    private final RaftCluster cluster;

    private final ServerStateMachine stateMachine;

    private final MemberStateManager memberStateManager;

    private final EventExecutor eventExecutor;

    private final Map<RaftState, ServerState> serverStates = new HashMap<>();
    private volatile RaftState state = FOLLOWER;

    private ServerContext(TransportClient client, RaftConfig raftConfig, SnapshotManager snapshotManager,
                          Log log, RaftMember self, RaftCluster cluster, ServerStateMachine stateMachine,
                          MemberStateManager memberStateManager, EventExecutor eventExecutor) {
        this.client = client;
        this.raftConfig = raftConfig;
        this.snapshotManager = snapshotManager;
        this.log = log;
        this.self = self;
        this.cluster = cluster;
        this.stateMachine = stateMachine;
        this.memberStateManager = memberStateManager;
        this.eventExecutor = eventExecutor;
    }

    public RaftCluster getCluster() {
        return cluster;
    }


    public SnapshotManager getSnapshotManager() {
        return snapshotManager;
    }


    public Log getLog() {
        return log;
    }


    public RaftMember getSelf() {
        return self;
    }


    public ServerStateMachine getStateMachine() {
        return stateMachine;
    }

    public RaftConfig getRaftConfig() {
        return raftConfig;
    }

    public MemberStateManager getMemberStateManager() {
        return memberStateManager;
    }

    public EventExecutor getEventExecutor() {
        return eventExecutor;
    }

    public TransportClient getClient() {
        return client;
    }

    public RaftState getState() {
        return state;
    }

    public ServerState getServerState() {
        return serverStates.get(state);
    }

    public void add(RaftState state, ServerState serverState) {
        serverStates.put(state, serverState);
    }

    public synchronized void transition(RaftState newState) {
        logger.info("{} transition to new state:{},old state:{}", self.getNodeId(), newState, state);

        ServerState serverState = serverStates.get(state);
        serverState.suspect();
        serverState = serverStates.get(newState);
        serverState.wakeup();

        this.state = newState;
    }


    public void onInstallComplete(Snapshot snapshot) {
        state = state.onInstallComplete(snapshot, this);
    }


    public void onHeartbeatTimeout() {
        state = state.onHeartbeatTimeout(this);
    }


    public void onElectTimeout() {
        state = state.onElectTimeout(this);
    }


    public void onElectAsMaster() {
        state = state.onElectAsMaster(this);
    }


    public void onNewLeader() {
        state = state.onNewLeader(this);
    }


    public void onFallBehind() {
        state = state.onFallBehind(this);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private TransportClient client;

        private RaftConfig raftConfig;

        private SnapshotManager snapshotManager;

        private Log log;

        private RaftMember self;

        private RaftCluster cluster;

        private ServerStateMachine stateMachine;

        private MemberStateManager memberStateManager;

        private EventExecutor eventExecutor;

        public Builder setClient(TransportClient client) {
            this.client = client;
            return this;
        }

        public Builder setRaftConfig(RaftConfig raftConfig) {
            this.raftConfig = raftConfig;
            return this;
        }

        public Builder setSnapshotManager(SnapshotManager snapshotManager) {
            this.snapshotManager = snapshotManager;
            return this;
        }

        public Builder setLog(Log log) {
            this.log = log;
            return this;
        }

        public Builder setSelf(RaftMember self) {
            this.self = self;
            return this;
        }

        public Builder setCluster(RaftCluster cluster) {
            this.cluster = cluster;
            return this;
        }

        public Builder setStateMachine(ServerStateMachine stateMachine) {
            this.stateMachine = stateMachine;
            return this;
        }

        public Builder setMemberStateManager(MemberStateManager memberStateManager) {
            this.memberStateManager = memberStateManager;
            return this;
        }

        public Builder setEventExecutor(EventExecutor eventExecutor) {
            this.eventExecutor = eventExecutor;
            return this;
        }

        public ServerContext build() {
            return new ServerContext(client, raftConfig, snapshotManager, log, self, cluster, stateMachine, memberStateManager, eventExecutor);
        }
    }
}

package com.vein.raft.server;


import com.vein.raft.server.events.EventExecutor;
import com.vein.raft.server.state.ServerState;
import com.vein.raft.server.storage.logs.RaftLogger;
import com.vein.raft.server.storage.state.MemberStateManager;
import com.vein.raft.server.storage.snapshot.Snapshot;
import com.vein.raft.server.storage.snapshot.SnapshotManager;
import com.vein.common.base.LoggerSupport;
import com.vein.transport.api.TransportClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 下午11:00
 */
public class ServerContext extends LoggerSupport {

    private final TransportClient client;

    private final RaftConfig raftConfig;

    private final SnapshotManager snapshotManager;

    private final RaftLogger raftLogger;

    private final RaftMember self;

    private final RaftCluster cluster;

    private final ServerStateMachine stateMachine;

    private final MemberStateManager memberStateManager;

    private final EventExecutor eventExecutor;

    private final Map<RaftState, ServerState> serverStates = new HashMap<>();
    private volatile RaftState state = RaftState.FOLLOWER;

    private ServerContext(TransportClient client, RaftConfig raftConfig, SnapshotManager snapshotManager,
                          RaftLogger raftLogger, RaftMember self, RaftCluster cluster, ServerStateMachine stateMachine,
                          MemberStateManager memberStateManager, EventExecutor eventExecutor) {
        this.client = client;
        this.raftConfig = raftConfig;
        this.snapshotManager = snapshotManager;
        this.raftLogger = raftLogger;
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

    public RaftLogger getRaftLogger() {
        return raftLogger;
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

    public synchronized ServerState getServerState() {
        return serverStates.get(state);
    }

    public void add(RaftState state, ServerState serverState) {
        serverStates.put(state, serverState);
    }

    public synchronized void transition(RaftState newState) {
//        logger.info("{} transition to old state:{},new state:{}", self.getNodeId(), state, newState);

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
        logger.info("member:{} onHeartbeatTimeout",self.getNodeId());
        state = state.onHeartbeatTimeout(this);
    }


    public void onElectTimeout() {
        logger.info("member:{} onElectTimeout",self.getNodeId());
        state = state.onElectTimeout(this);
    }


    public void onElectAsMaster() {
        logger.info("member:{} onElectAsMaster",self.getNodeId());
        state = state.onElectAsMaster(this);
    }


    public void onNewLeader() {
        logger.info("member:{} onNewLeader",self.getNodeId());
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

        private RaftLogger log;

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

        public Builder setRaftLogger(RaftLogger log) {
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

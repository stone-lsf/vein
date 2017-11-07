package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftMember;
import com.sm.charge.raft.server.RaftState;
import com.sm.charge.raft.server.ServerContext;
import com.sm.charge.raft.server.events.VoteRequest;
import com.sm.charge.raft.server.events.VoteResponse;
import com.sm.charge.raft.server.state.support.VoteQuorum;
import com.sm.charge.raft.server.storage.logs.RaftLogger;
import com.sm.charge.raft.server.storage.logs.entry.LogEntry;
import com.sm.charge.raft.server.storage.state.MemberStateManager;
import com.sm.charge.raft.server.state.support.timer.ElectTimeoutTimer;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.handler.AbstractResponseHandler;
import com.sm.finance.charge.transport.api.support.ResponseContext;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:50
 */
public class CandidateState extends AbstractState {

    private final ElectTimeoutTimer timer;

    public CandidateState(ServerContext context, ElectTimeoutTimer timer) {
        super(context);
        this.timer = timer;
    }

    @Override
    public RaftState state() {
        return RaftState.CANDIDATE;
    }

    @Override
    public void suspect() {
        timer.stop();
        self.getState().clearVoteQuorum();
    }

    @Override
    public void wakeup() {
        logger.info("{} transfer to candidate state", self.getNodeId());
        timer.start();

        self.setTerm(self.getTerm() + 1);
        self.setVotedFor(self.getNodeId());

        int quorum = context.getCluster().getQuorum();

        VoteQuorum voteQuorum = new VoteQuorum(quorum, context::onElectAsMaster);

        voteQuorum.mergeSuccess();
        self.getState().setVoteQuorum(voteQuorum);

        MemberStateManager manager = context.getMemberStateManager();
        manager.persistState(self);

        requestVotes(voteQuorum);
    }


    private void requestVotes(VoteQuorum voteQuorum) {
        List<RaftMember> members = context.getCluster().members();
        RaftLogger raftLogger = context.getRaftLogger();
        LogEntry entry = raftLogger.lastEntry();
        long lastIndex = entry == null ? 0 : entry.getIndex();
        long lastTerm = entry == null ? 0 : entry.getTerm();
        long term = self.getTerm();

        for (RaftMember member : members) {
            String memberId = member.getNodeId();
            if (memberId.equals(self.getNodeId())) {
                continue;
            }

            Connection connection = member.getState().getConnection();
            if (connection == null) {
                voteQuorum.mergeFailure();
                continue;
            }

            VoteRequest request = buildRequest(lastIndex, lastTerm, term, memberId);
            connection.send(request, new AbstractResponseHandler<VoteResponse>() {
                @Override
                public void handle(VoteResponse response, Connection connection) {
                    eventExecutor.execute(response);
                }

                @Override
                public void onException(Throwable e, ResponseContext context) {
                    logger.error("send vote request to:{} caught exception", member.getNodeId(), e);
                    voteQuorum.mergeFailure();
                }
            });
        }
    }

    private VoteRequest buildRequest(long lastIndex,long lastTerm,long term,String destination){
        VoteRequest request = new VoteRequest();
        request.setLastLogIndex(lastIndex);
        request.setLastLogTerm(lastTerm);
        request.setSource(self.getNodeId());
        request.setTerm(term);
        request.setDestination(destination);

        return request;
    }

    @Override
    public void handle(VoteResponse response) {
        long responseTerm = response.getTerm();
        if (updateTerm(responseTerm)) {
            return;
        }

        VoteQuorum quorum = self.getState().getVoteQuorum();

        if (response.isVoteGranted()) {
            quorum.mergeSuccess();
        } else {
            quorum.mergeFailure();
        }
    }
}

package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftListener;
import com.sm.charge.raft.server.RaftMember;
import com.sm.charge.raft.server.RaftState;
import com.sm.charge.raft.server.ServerContext;
import com.sm.charge.raft.server.election.VoteQuorum;
import com.sm.charge.raft.server.election.VoteRequest;
import com.sm.charge.raft.server.election.VoteResponse;
import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.charge.raft.server.storage.MemberStateManager;
import com.sm.charge.raft.server.timer.ElectTimeoutTimer;
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

    public CandidateState(RaftListener raftListener, ServerContext context, ElectTimeoutTimer timer) {
        super(raftListener, context);
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
        timer.start();

        self.setTerm(self.getTerm() + 1);
        self.setVotedFor(self.getId());

        int quorum = context.getCluster().getQuorum();
        VoteQuorum voteQuorum = new VoteQuorum(quorum);
        voteQuorum.mergeSuccess();
        self.getState().setVoteQuorum(voteQuorum);

        MemberStateManager manager = context.getMemberStateManager();
        manager.persistState(self);

        requestVotes(voteQuorum);
    }


    private void requestVotes(VoteQuorum voteQuorum) {
        List<RaftMember> members = context.getCluster().members();
        Log log = context.getLog();
        LogEntry entry = log.lastEntry();
        long lastIndex = entry == null ? 0 : entry.getIndex();
        long lastTerm = entry == null ? 0 : entry.getTerm();

        VoteRequest request = new VoteRequest();
        request.setLastLogIndex(lastIndex);
        request.setLastLogTerm(lastTerm);
        request.setSource(context.getSelf().getId());
        request.setTerm(context.getSelf().getTerm());

        for (RaftMember member : members) {
            if (member.getId() == context.getSelf().getId()) {
                continue;
            }

            Connection connection = member.getState().getConnection();
            if (connection == null) {
                voteQuorum.mergeFailure();
                continue;
            }

            request.setDestination(member.getId());
            connection.send(request, new AbstractResponseHandler<VoteResponse>() {
                @Override
                public void handle(VoteResponse response, Connection connection) {
                    eventExecutor.execute(response);
                }

                @Override
                public void onException(Throwable e, ResponseContext context) {
                    logger.error("send vote request to:{} caught exception", member.getId(), e);
                    voteQuorum.mergeFailure();
                }
            });
        }
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

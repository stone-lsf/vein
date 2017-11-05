package com.sm.charge.raft.server.state;

import com.sm.charge.raft.client.ConfigureCommand;
import com.sm.charge.raft.server.RaftCluster;
import com.sm.charge.raft.server.RaftMember;
import com.sm.charge.raft.server.RaftMemberState;
import com.sm.charge.raft.server.RaftState;
import com.sm.charge.raft.server.ServerContext;
import com.sm.charge.raft.server.events.InstallSnapshotResponse;
import com.sm.charge.raft.server.events.JoinRequest;
import com.sm.charge.raft.server.events.JoinResponse;
import com.sm.charge.raft.server.events.LeaveRequest;
import com.sm.charge.raft.server.events.LeaveResponse;
import com.sm.charge.raft.server.events.AppendRequest;
import com.sm.charge.raft.server.events.AppendResponse;
import com.sm.charge.raft.server.state.support.SnapshotInstallContext;
import com.sm.charge.raft.server.state.support.Replicator;
import com.sm.charge.raft.server.storage.logs.RaftLogger;
import com.sm.charge.raft.server.storage.logs.entry.LogEntry;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.sm.charge.raft.server.events.JoinResponse.INTERNAL_ERROR;
import static com.sm.charge.raft.server.events.JoinResponse.RECONFIGURING;
import static com.sm.charge.raft.server.events.JoinResponse.SUCCESS;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:50
 */
public class LeaderState extends AbstractState {
    private final Replicator replicator;

    public LeaderState(ServerContext context, Replicator replicator) {
        super(context);
        this.replicator = replicator;
    }

    @Override
    public RaftState state() {
        return RaftState.LEADER;
    }

    @Override
    protected AppendResponse doHandle(AppendRequest request) {
        logger.error("receive append request from another leader[{}] with same term, there must be a bug, com.sm.charge.raft.server exits", request.getSource());
        System.exit(-1);
        throw new IllegalStateException("more than one master has the same term:" + self.getTerm());
    }

    @Override
    public void suspect() {
        RaftMember self = context.getSelf();
        List<RaftMember> members = context.getCluster().members();
        for (RaftMember member : members) {
            if (member.getNodeId() == self.getNodeId()) {
                continue;
            }

            member.getState().stopReplicate();
        }
    }

    @Override
    public void wakeup() {
        logger.info("{} transfer to leader state", self.getNodeId());
        RaftMember self = context.getSelf();
        List<RaftMember> members = context.getCluster().members();
        LogEntry entry = context.getLog().lastEntry();
        long nextLogIndex = entry == null ? 1 : entry.getIndex() + 1;

        for (RaftMember member : members) {
            if (member.getNodeId() == self.getNodeId()) {
                continue;
            }

            member.setNextLogIndex(nextLogIndex);
            member.getState().startReplicate();
        }
    }

    @Override
    public void handle(AppendResponse response) {
        long responseTerm = response.getTerm();
        if (updateTerm(responseTerm)) {
            return;
        }

        RaftMember member = context.getCluster().member(response.getSource());
        if (response.isSuccess()) {
            member.setNextLogIndex(response.getNextIndex());
            member.setMatchedIndex(response.getNextIndex() - 1);

            List<RaftMember> members = context.getCluster().members();
            members.sort(Comparator.comparingLong(RaftMember::getMatchedIndex));
            int quorum = context.getCluster().getQuorum();
            if (members.size() < quorum) {
                return;
            }
            RaftMember m = members.get(quorum - 1);
            long commitIndex = m.getMatchedIndex();
            commit(commitIndex);
        } else {
            member.setNextLogIndex(response.getNextIndex());
        }
    }

    @Override
    public JoinResponse handle(JoinRequest request, RequestContext requestContext) {
        JoinResponse response = new JoinResponse();
        response.setTerm(self.getTerm());

        if (self.getState().getConfiguring() > 0) {
            response.setStatus(RECONFIGURING);
            return response;
        }

        RaftCluster cluster = context.getCluster();
        List<RaftMember> members = cluster.members();
        RaftMember member = cluster.member(request.getMemberId());
        if (member != null) {
            response.setStatus(SUCCESS);
            response.setIndex(context.getLog().lastIndex());
            response.setTerm(self.getTerm());
            response.setMembers(members);
            return response;
        }

        member = new RaftMember(context.getClient(), request.getMemberId(), request.getAddress(), replicator);
        members.add(member);

        configure(members).whenComplete((index, error) -> {
            try {
                if (error != null) {
                    response.setStatus(INTERNAL_ERROR);
                    requestContext.sendResponse(response);
                } else {
                    response.setStatus(SUCCESS);
                    response.setIndex(index);
                    response.setTerm(self.getTerm());
                    response.setMembers(members);
                    requestContext.sendResponse(response);
                }
            } catch (Exception e) {
                logger.error("send join response:{} failure", response, e);
            }
        });
        return null;
    }

    private CompletableFuture<Long> configure(List<RaftMember> members) {
        CompletableFuture<Long> future = new CompletableFuture<>();

        ConfigureCommand command = new ConfigureCommand();
        LogEntry entry = new LogEntry(command, self.getTerm());
        RaftLogger log = context.getLog();
        RaftCluster cluster = context.getCluster();
        long index = log.append(entry);
        self.getState().setConfiguring(index);

        for (RaftMember member : members) {
            if (!cluster.contain(member.getNodeId())) {
                cluster.add(member);
                member.setNextLogIndex(1);
                member.getState().startReplicate();
                logger.info("com.sm.charge.raft.server[id:{};address:{}] is add to cluster,log index:{}", member.getNodeId(), member.getAddress(), index);
            }
        }

        CompletableFuture<Object> commitFuture = new CompletableFuture<>();
        commitFuture.whenComplete((result, error) -> {
            if (error == null) {
                future.complete(index);
            } else {
                future.completeExceptionally(error);
            }
        });
        self.getState().addCommitFuture(index, commitFuture);
        return future;
    }


    @Override
    public LeaveResponse handle(LeaveRequest request, RequestContext requestContext) {
        LeaveResponse response = new LeaveResponse();
        fill(response, request.getSource());
        if (self.getState().getConfiguring() > 0) {
            response.setStatus(RECONFIGURING);
            return response;
        }

        RaftCluster cluster = context.getCluster();
        List<RaftMember> members = cluster.members();
        RaftMember member = cluster.member(request.getSource());
        if (member == null) {
            response.setStatus(SUCCESS);
            return response;
        }

        members.remove(member);
        configure(members).whenComplete((index, error) -> {
            try {
                if (error != null) {
                    response.setStatus(LeaveResponse.INTERNAL_ERROR);
                    requestContext.sendResponse(response);
                } else {
                    response.setStatus(LeaveResponse.SUCCESS);
                    requestContext.sendResponse(response);
                }
            } catch (Exception e) {
                logger.error("send join response:{} failure", response, e);
            }
        });
        return null;
    }

    @Override
    public void handle(InstallSnapshotResponse response) {
        long requestTerm = response.getTerm();
        if (updateTerm(requestTerm)) {
            return;
        }

        RaftMember member = context.getCluster().member(response.getSource());
        if (member == null) {
            return;
        }
        RaftMemberState state = member.getState();

        if (response.isAccepted()) {
            state.setNextSnapshotOffset(response.getNextOffset());
            return;
        }

        SnapshotInstallContext installContext = state.getInstallContext();
        if (installContext.isComplete()) {
            return;
        }
        installContext.setOffset(installContext.getOffset() + installContext.getSize());
    }
}

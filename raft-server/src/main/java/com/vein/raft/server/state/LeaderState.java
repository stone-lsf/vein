package com.vein.raft.server.state;

import com.google.common.collect.Lists;

import com.vein.raft.client.Configure;
import com.vein.raft.server.RaftCluster;
import com.vein.raft.server.RaftMember;
import com.vein.raft.server.RaftMemberState;
import com.vein.raft.server.RaftState;
import com.vein.raft.server.ServerContext;
import com.vein.raft.server.events.AppendRequest;
import com.vein.raft.server.events.AppendResponse;
import com.vein.raft.server.events.InstallSnapshotResponse;
import com.vein.raft.server.events.JoinRequest;
import com.vein.raft.server.events.JoinResponse;
import com.vein.raft.server.events.LeaveRequest;
import com.vein.raft.server.events.LeaveResponse;
import com.vein.raft.server.events.MemberInfo;
import com.vein.raft.server.state.support.Replicator;
import com.vein.raft.server.state.support.SnapshotInstallContext;
import com.vein.raft.server.storage.logs.RaftLogger;
import com.vein.raft.server.storage.logs.entry.LogEntry;
import com.vein.transport.api.support.RequestContext;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.vein.raft.server.events.JoinResponse.INTERNAL_ERROR;
import static com.vein.raft.server.events.JoinResponse.RECONFIGURING;
import static com.vein.raft.server.events.JoinResponse.SUCCESS;

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
            if (member.getNodeId().equals(self.getNodeId())) {
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
        LogEntry entry = context.getRaftLogger().lastEntry();
        long nextLogIndex = entry == null ? 1 : entry.getIndex() + 1;

        for (RaftMember member : members) {
            if (member.getNodeId().equals(self.getNodeId())) {
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
        logger.info("handle join request:{}", request);
        JoinResponse response = new JoinResponse();
        response.setTerm(self.getTerm());

        if (self.getState().getConfiguring() > 0) {
            logger.info("cluster is reconfiguring");
            response.setStatus(RECONFIGURING);
            return response;
        }

        RaftCluster cluster = context.getCluster();
        List<RaftMember> members = cluster.members();
        String memberId = request.getMemberId();
        RaftMember member = cluster.member(memberId);
        if (member != null) {
            logger.info("handle join request:{} success", request);
            response.setStatus(SUCCESS);
            response.setIndex(context.getRaftLogger().lastIndex());
            response.setTerm(self.getTerm());
            response.setMembers(Lists.transform(members, MemberInfo::new));
            member.getState().startReplicate();
            return response;
        }

        logger.info("cluster add new member:{}", memberId);
        member = new RaftMember(context.getClient(), memberId, request.getAddress(), replicator);
        members.add(member);

        Configure configure = new Configure(Configure.JOIN, memberId, request.getAddress());
        configure(members, configure).whenComplete((index, error) -> {
            try {
                if (error != null) {
                    response.setStatus(INTERNAL_ERROR);
                    requestContext.sendResponse(response);
                } else {
                    response.setStatus(SUCCESS);
                    response.setIndex(index);
                    response.setTerm(self.getTerm());
                    response.setMembers(Lists.transform(members, MemberInfo::new));
                    requestContext.sendResponse(response);
                }
            } catch (Exception e) {
                logger.error("send join response:{} failure", response, e);
            }
        });
        return null;
    }

    private CompletableFuture<Long> configure(List<RaftMember> members, Configure configure) {
        CompletableFuture<Long> future = new CompletableFuture<>();


        LogEntry entry = new LogEntry(configure, self.getTerm());
        RaftLogger raftLogger = context.getRaftLogger();
        RaftCluster cluster = context.getCluster();
        long index = raftLogger.append(entry);
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
        Configure configure = new Configure(Configure.LEAVE, member.getNodeId(), member.getAddress());
        configure(members, configure).whenComplete((index, error) -> {
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

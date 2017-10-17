package com.sm.charge.raft.server.state;

import com.sm.charge.raft.client.ConfigureCommand;
import com.sm.charge.raft.server.RaftCluster;
import com.sm.charge.raft.server.RaftListener;
import com.sm.charge.raft.server.RaftMember;
import com.sm.charge.raft.server.RaftState;
import com.sm.charge.raft.server.ServerContext;
import com.sm.charge.raft.server.membership.JoinRequest;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.replicate.AppendRequest;
import com.sm.charge.raft.server.replicate.AppendResponse;
import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.charge.raft.server.storage.Snapshot;
import com.sm.finance.charge.common.NamedThreadFactory;
import com.sm.finance.charge.common.SystemConstants;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.handler.AbstractResponseHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;
import com.sm.finance.charge.transport.api.support.ResponseContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.sm.charge.raft.server.membership.JoinResponse.INTERNAL_ERROR;
import static com.sm.charge.raft.server.membership.JoinResponse.RECONFIGURING;
import static com.sm.charge.raft.server.membership.JoinResponse.SUCCESS;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:50
 */
public class LeaderState extends AbstractState {
    private final ExecutorService executor = Executors.newFixedThreadPool(SystemConstants.PROCESSORS, new NamedThreadFactory("AppendPoll"));

    private final int maxAppendSize;

    public LeaderState(RaftListener raftListener, ServerContext context, int maxAppendSize) {
        super(raftListener, context);
        this.maxAppendSize = maxAppendSize;
    }

    @Override
    public RaftState state() {
        return RaftState.LEADER;
    }

    @Override
    protected AppendResponse doHandle(AppendRequest request) {
        logger.error("receive append request from another leader[{}] with same term, there must be a bug, server exits", request.getSource());
        System.exit(-1);
        throw new IllegalStateException("more than one master has the same term:" + self.getTerm());
    }

    @Override
    public void suspect() {
        List<RaftMember> members = context.getCluster().members();
        for (RaftMember member : members) {
            if (member.getId() == self.getId()) {
                continue;
            }

            Future<?> future = member.getContext().getAppendFuture();
            if (future != null) {
                future.cancel(false);
            }
        }
    }

    @Override
    public void wakeup() {
        context.getCluster().setMaster(context.getSelf());
        List<RaftMember> members = context.getCluster().members();
        LogEntry entry = context.getLog().lastEntry();
        long nextLogIndex = entry == null ? 1 : entry.getIndex() + 1;

        for (RaftMember member : members) {
            if (member.getId() == self.getId()) {
                continue;
            }

            member.setNextLogIndex(nextLogIndex);
            Future<?> future = executor.submit(() -> replicateEntries(member));
            member.getContext().setAppendFuture(future);
        }
    }

    private void replicateEntries(RaftMember member) {
        logger.info("leader append entry to member:[{}]", member.getId());
        Connection connection = member.getContext().getConnection();
        if (connection == null) {
            return;
        }

        Snapshot snapshot = context.getSnapshotManager().currentSnapshot();
        long nextLogIndex = member.getNextLogIndex();

        if (snapshot != null && nextLogIndex < snapshot.index()) {
            sendSnapshot(member);
            return;
        }

        long prevLogIndex = 0;
        long prevLogTerm = 0;
        RaftMember leader = context.getSelf();
        long leaderCommit = leader.getCommitIndex();
        Log log = context.getLog();
        LogEntry entry = log.get(nextLogIndex - 1);
        if (entry != null) {
            prevLogIndex = entry.getIndex();
            prevLogTerm = entry.getTerm();
        }

        LogEntry lastLogEntry = log.lastEntry();
        long lastLogIndex = lastLogEntry == null ? 0 : lastLogEntry.getIndex();

        AppendRequest request = new AppendRequest();
        request.setDestination(member.getId());
        request.setSource(leader.getId());
        request.setTerm(leader.getTerm());
        request.setPrevLogIndex(prevLogIndex);
        request.setPrevLogTerm(prevLogTerm);
        request.setLeaderCommit(leaderCommit);

        long endIndex = Math.min(lastLogIndex, prevLogIndex + 1 + maxAppendSize);
        ArrayList<LogEntry> entries = new ArrayList<>();
        for (; nextLogIndex < endIndex; nextLogIndex++) {
            LogEntry logEntry = log.get(nextLogIndex);
            if (logEntry != null) {
                entries.add(logEntry);
            }
        }

        request.setEntries(entries);

        connection.send(request, new AbstractResponseHandler<AppendResponse>() {
            @Override
            public void handle(AppendResponse response, Connection connection) {
                context.getEventExecutor().execute(response);
            }

            @Override
            public void onException(Throwable e, ResponseContext context) {
                logger.error("send append request to member:{} caught exception", member.getId(), e);
            }
        });
    }

    private void sendSnapshot(RaftMember member) {

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
        if (self.getConfiguring() > 0) {
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

        member = new RaftMember(context.getClient(), request.getMemberId(), request.getAddress());
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
        Log log = context.getLog();
        long index = log.append(entry);

        CompletableFuture<Object> commitFuture = new CompletableFuture<>();
        commitFuture.whenComplete((result, error) -> {
            if (error == null) {
                future.complete(index);
            } else {
                future.completeExceptionally(error);
            }
        });
        self.getContext().addCommitFuture(index, commitFuture);
        return future;
    }
}

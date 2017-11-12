package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftMember;
import com.sm.charge.raft.server.RaftMessage;
import com.sm.charge.raft.server.ServerContext;
import com.sm.charge.raft.server.events.AppendRequest;
import com.sm.charge.raft.server.events.AppendResponse;
import com.sm.charge.raft.server.events.EventExecutor;
import com.sm.charge.raft.server.events.InstallSnapshotRequest;
import com.sm.charge.raft.server.events.InstallSnapshotResponse;
import com.sm.charge.raft.server.events.JoinRequest;
import com.sm.charge.raft.server.events.JoinResponse;
import com.sm.charge.raft.server.events.LeaveRequest;
import com.sm.charge.raft.server.events.LeaveResponse;
import com.sm.charge.raft.server.events.MemberInfo;
import com.sm.charge.raft.server.events.VoteRequest;
import com.sm.charge.raft.server.events.VoteResponse;
import com.sm.charge.raft.server.storage.logs.RaftLogger;
import com.sm.charge.raft.server.storage.logs.entry.LogEntry;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.support.RequestContext;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.sm.charge.raft.server.events.JoinResponse.NO_LEADER;
import static com.sm.charge.raft.server.events.JoinResponse.REDIRECT;
import static com.sm.charge.raft.server.events.LeaveResponse.LOWER_TERM;

/**
 * @author shifeng.luo
 * @version created on 2017/10/13 下午4:25
 */
public abstract class AbstractState extends LoggerSupport implements ServerState {

    protected final RaftMember self;
    protected final ServerContext context;
    protected final EventExecutor eventExecutor;

    AbstractState(ServerContext context) {
        this.self = context.getSelf();
        this.context = context;
        this.eventExecutor = context.getEventExecutor();
    }

    @Override
    public VoteResponse handle(VoteRequest request) {
        long requestTerm = request.getTerm();
        VoteResponse response = new VoteResponse();
        response.setSource(self.getNodeId());
        response.setDestination(request.getSource());

        if (updateTerm(requestTerm)) {
            response.setTerm(self.getTerm());
            self.setVotedFor(request.getSource());
            response.setVoteGranted(true);
            return response;
        }

        long currentTerm = self.getTerm();
        response.setTerm(currentTerm);
        if (requestTerm < currentTerm) {
            response.setVoteGranted(false);
            return response;
        }

        if (self.getVotedFor() != null && !request.getSource().equals(self.getVotedFor())) {
            response.setVoteGranted(false);
            return response;
        }

        if (!candidateLogNewer(request.getLastLogIndex())) {
            response.setVoteGranted(false);
            return response;
        }

        self.setVotedFor(request.getSource());
        context.getMemberStateManager().persistState(self);
        response.setVoteGranted(true);
        return response;
    }

    /**
     * 比较当前term与消息的term，判断是否落后
     *
     * @param messageTerm 消息term
     * @return 如果当前任期新则返回false，否则返回true
     */
    boolean updateTerm(long messageTerm) {
        long term = self.getTerm();
        if (term < messageTerm) {
            self.setTerm(messageTerm);
            self.setVotedFor(null);
            context.onFallBehind();
            return true;
        }

        return false;
    }

    /**
     * 判断候选节点的日志是否更新
     *
     * @param candidateLogIndex 候选者日志索引
     * @return 如果候选者更新，返回true，否则返回false
     */
    private boolean candidateLogNewer(long candidateLogIndex) {
        LogEntry entry = context.getRaftLogger().lastEntry();
        long lastLogIndex = entry == null ? 0 : entry.getIndex();

        return candidateLogIndex >= lastLogIndex;
    }

    @Override
    public void handle(VoteResponse response) {
        long responseTerm = response.getTerm();
        updateTerm(responseTerm);
    }

    @Override
    public AppendResponse handle(AppendRequest request) {
        long requestTerm = request.getTerm();
        updateTerm(requestTerm);

        if (requestTerm < self.getTerm()) {
            AppendResponse response = new AppendResponse();
            fill(response, request.getSource());
            response.setSuccess(false);
            return response;
        }

        return doHandle(request);
    }

    protected AppendResponse doHandle(AppendRequest request) {
        long prevLogIndex = request.getPrevLogIndex();
        if (prevLogIndex > 0) {
            AppendResponse response = appendWithPrevLog(prevLogIndex, request);
            if (response != null) {
                return response;
            }
        }

        context.getRaftLogger().truncate(0);
        logger.info("delete all logs,because receive prev log 0 from master[{}]", request.getSource());
        return appendInitial(request);
    }

    /**
     * 检查前一条日志是否匹配
     *
     * @param prevLogIndex 前一条日志的index
     * @param request      append请求
     * @return AppendResponse
     */
    private AppendResponse appendWithPrevLog(long prevLogIndex, AppendRequest request) {
        LogEntry prevEntry = context.getRaftLogger().get(prevLogIndex);
        LogEntry lastLogEntry = context.getRaftLogger().lastEntry();
        String source = request.getSource();

        if (prevEntry == null) {
            logger.info("append entry failure,because received pre index:{} log from master[{}] isn't exist!", prevLogIndex, source);
            AppendResponse response = new AppendResponse();
            fill(response, source);
            response.setNextIndex(lastLogEntry == null ? 1 : lastLogEntry.getIndex() + 1);
            response.setSuccess(false);
            return response;
        }

        long entryTerm = prevEntry.getTerm();
        if (entryTerm != request.getPrevLogTerm()) {
            logger.info("append entry failure,because received pre index:{} log's term:{} don't eq master's[{}] term:{}", prevLogIndex, entryTerm, source, prevLogIndex);
            //TODO 优化返回的nextIndex，此时只能一步一步回退
            AppendResponse response = new AppendResponse();
            fill(response, source);
            response.setNextIndex(lastLogEntry == null ? 1 : prevEntry.getIndex());
            response.setSuccess(false);
            return response;
        }

        context.getRaftLogger().truncate(prevLogIndex);
        logger.info("delete logs from index greater than {},because receive prev log from master[{}]", prevLogIndex, source);
        return appendInitial(request);
    }

    /**
     * 追加日志
     *
     * @param request append request
     * @return AppendResponse
     */
    private AppendResponse appendInitial(AppendRequest request) {
        String source = request.getSource();
        List<LogEntry> entries = request.getEntries();
        RaftLogger raftLogger = context.getRaftLogger();
        long lastIndex = 0;
        if (CollectionUtils.isNotEmpty(entries)) {
            for (LogEntry entry : entries) {
                long skipSize = entry.getIndex() - lastIndex - 1;
                raftLogger.skip(skipSize);
                raftLogger.append(entry);
                lastIndex = entry.getIndex();
            }
        }

        commit(request.getLeaderCommit());
        context.getStateMachine().apply(self.getCommitIndex());

        AppendResponse response = new AppendResponse();
        fill(response, source);
        response.setSuccess(true);
        response.setNextIndex(lastIndex + 1);
        return response;
    }

    /**
     * 提交日志
     *
     * @param leaderCommit leader已经提交的日志
     */
    void commit(long leaderCommit) {
        long lastIndex = context.getRaftLogger().lastIndex();
        long commitIndex = Math.min(lastIndex, leaderCommit);
        long prevCommitIndex = self.getCommitIndex();
        if (commitIndex > prevCommitIndex) {
            context.getRaftLogger().commit(commitIndex);
            self.setCommitIndex(commitIndex);
        }
    }

    @Override
    public void handle(AppendResponse response) {
        long responseTerm = response.getTerm();
        updateTerm(responseTerm);
    }

    @Override
    public JoinResponse handle(JoinRequest request, RequestContext requestContext) {
        logger.info("handle join request:{}", request);
        RaftMember master = context.getCluster().master();
        JoinResponse response = new JoinResponse();
        response.setTerm(self.getTerm());
        if (master == null) {
            logger.info("cluster don't have leader,join member:{}", request.getMemberId());
            response.setStatus(NO_LEADER);
            return response;
        }

        logger.info("redirect to leader:{},join member:{}", master.getNodeId(), request.getMemberId());
        response.setStatus(REDIRECT);
        response.setMaster(new MemberInfo(master));
        return response;
    }

    @Override
    public void handle(JoinResponse response) {
        long responseTerm = response.getTerm();
        updateTerm(responseTerm);
    }

    @Override
    public LeaveResponse handle(LeaveRequest request, RequestContext requestContext) {
        long requestTerm = request.getTerm();
        LeaveResponse response = new LeaveResponse();
        fill(response, request.getSource());

        if (updateTerm(requestTerm)) {
            response.setStatus(LOWER_TERM);
            return response;
        }

        RaftMember master = context.getCluster().master();
        if (master == null) {
            response.setStatus(NO_LEADER);
            return response;
        }

        response.setStatus(REDIRECT);
        response.setMaster(master);
        return response;
    }

    @Override
    public void handle(LeaveResponse response) {
        long responseTerm = response.getTerm();
        updateTerm(responseTerm);
    }

    @Override
    public InstallSnapshotResponse handle(InstallSnapshotRequest request) {
        long requestTerm = request.getTerm();
        updateTerm(requestTerm);

        InstallSnapshotResponse response = new InstallSnapshotResponse();
        fill(response, request.getSource());
        response.setAccepted(false);
        return response;
    }

    @Override
    public void handle(InstallSnapshotResponse response) {
        long requestTerm = response.getTerm();
        updateTerm(requestTerm);
    }


    void fill(RaftMessage message, String destination) {
        message.setSource(self.getNodeId());
        message.setDestination(destination);
        message.setTerm(self.getTerm());
    }
}

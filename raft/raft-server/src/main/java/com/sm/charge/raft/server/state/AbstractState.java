package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftMemberState;
import com.sm.charge.raft.server.RaftMemberStateManager;
import com.sm.charge.raft.server.RaftMessage;
import com.sm.charge.raft.server.RaftStateMachine;
import com.sm.charge.raft.server.ServerStateMachine;
import com.sm.charge.raft.server.election.VoteRequest;
import com.sm.charge.raft.server.election.VoteResponse;
import com.sm.charge.raft.server.membership.ConfigurationRequest;
import com.sm.charge.raft.server.membership.ConfigurationResponse;
import com.sm.charge.raft.server.membership.InstallSnapshotRequest;
import com.sm.charge.raft.server.membership.InstallSnapshotResponse;
import com.sm.charge.raft.server.replicate.AppendRequest;
import com.sm.charge.raft.server.replicate.AppendResponse;
import com.sm.charge.raft.server.storage.Log;
import com.sm.charge.raft.server.storage.LogEntry;
import com.sm.finance.charge.common.LogSupport;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author shifeng.luo
 * @version created on 2017/10/13 下午4:25
 */
public abstract class AbstractState extends LogSupport implements ServerState {

    private final RaftMemberState memberState;
    private final RaftStateMachine stateMachine;
    private final Log log;
    private final RaftMemberStateManager stateManager;
    protected final ServerStateMachine serverStateMachine;

    protected AbstractState(RaftMemberState memberState, RaftStateMachine stateMachine, Log log, RaftMemberStateManager stateManager, ServerStateMachine serverStateMachine) {
        this.memberState = memberState;
        this.stateMachine = stateMachine;
        this.log = log;
        this.stateManager = stateManager;
        this.serverStateMachine = serverStateMachine;
    }

    @Override
    public VoteResponse handle(VoteRequest request) {
        long requestTerm = request.getTerm();
        VoteResponse response = new VoteResponse();
        response.setSource(memberState.getMember().getId());
        response.setDestination(request.getSource());

        if (updateTerm(requestTerm)) {
            response.setTerm(memberState.getTerm());
            memberState.setVotedFor(request.getSource());
            response.setVoteGranted(true);
            return response;
        }

        long currentTerm = memberState.getTerm();
        response.setTerm(currentTerm);
        if (requestTerm < currentTerm) {
            response.setVoteGranted(true);
            return response;
        }

        if (memberState.getVotedFor() > 0) {
            response.setVoteGranted(false);
            return response;
        }

        if (!candidateLogNewer(request.getLastLogIndex())) {
            response.setVoteGranted(false);
            return response;
        }

        memberState.setVotedFor(request.getSource());
        stateManager.persistState(memberState);
        response.setVoteGranted(true);
        return response;
    }

    /**
     * 比较当前term与消息的term，判断是否落后
     *
     * @param messageTerm 消息term
     * @return 如果当前任期新则返回false，否则返回true
     */
    protected boolean updateTerm(long messageTerm) {
        long term = memberState.getTerm();
        if (term < messageTerm) {
            memberState.setTerm(messageTerm);
            memberState.setVotedFor(-1);
            stateMachine.onFallBehind();
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
        LogEntry entry = log.lastEntry();
        long lastLogIndex = entry == null ? 0 : entry.getIndex();

        return candidateLogIndex >= lastLogIndex;
    }

    @Override
    public void handle(VoteResponse response) {

    }

    @Override
    public AppendResponse handle(AppendRequest request) {
        long requestTerm = request.getTerm();
        updateTerm(requestTerm);

        if (requestTerm < memberState.getTerm()) {
            AppendResponse response = new AppendResponse();
            fill(response, request.getSource());
            response.setSuccess(false);
            return response;
        }

        long prevLogIndex = request.getPrevLogIndex();
        if (prevLogIndex > 0) {
            AppendResponse response = appendWithPrevLog(prevLogIndex, request);
            if (response != null) {
                return response;
            }
        }

        log.truncate(0);
        logger.info("delete all logs,because receive prev log 0 from master[{}]", request.getSource());
        return appendEntries(request);
    }

    /**
     * 检查前一条日志是否匹配
     *
     * @param prevLogIndex 前一条日志的index
     * @param request      append请求
     * @return AppendResponse
     */
    private AppendResponse appendWithPrevLog(long prevLogIndex, AppendRequest request) {
        LogEntry prevEntry = log.get(prevLogIndex);
        LogEntry lastLogEntry = log.lastEntry();
        long source = request.getSource();

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

        log.truncate(prevLogIndex);
        logger.info("delete logs from index greater than {},because receive prev log from master[{}]", prevLogIndex, source);
        return appendEntries(request);
    }

    /**
     * 追加日志
     *
     * @param request append request
     * @return AppendResponse
     */
    private AppendResponse appendEntries(AppendRequest request) {
        long source = request.getSource();
        LogEntry[] entries = request.getEntries();
        long lastIndex = log.lastIndex();
        if (ArrayUtils.isNotEmpty(entries)) {
            for (LogEntry entry : entries) {
                long skipSize = entry.getIndex() - log.lastIndex() - 1;
                log.skip(skipSize);
                log.append(entry);
                lastIndex = entry.getIndex();
            }
        }

        commit(request.getLeaderCommit());
        serverStateMachine.apply(memberState.getCommitIndex());

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
    private void commit(long leaderCommit) {
        long lastIndex = log.lastIndex();
        long commitIndex = Math.min(lastIndex, leaderCommit);
        long prevCommitIndex = memberState.getCommitIndex();
        if (commitIndex > prevCommitIndex) {
            log.commit(commitIndex);
            memberState.setCommitIndex(commitIndex);
        }
    }

    @Override
    public void handle(AppendResponse response) {

    }

    @Override
    public ConfigurationResponse handle(ConfigurationRequest request) {

        return null;
    }

    @Override
    public void handle(ConfigurationResponse response) {

    }

    @Override
    public InstallSnapshotResponse handle(InstallSnapshotRequest request) {
        InstallSnapshotResponse response = new InstallSnapshotResponse();
        fill(response, request.getSource());
        response.setAccepted(false);
        return response;
    }

    @Override
    public void handle(InstallSnapshotResponse response) {

    }


    protected void fill(RaftMessage message, long destination) {
        message.setSource(memberState.getMember().getId());
        message.setDestination(destination);
        message.setTerm(memberState.getTerm());
    }
}

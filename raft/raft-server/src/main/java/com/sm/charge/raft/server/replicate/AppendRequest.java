package com.sm.charge.raft.server.replicate;

import com.sm.charge.raft.server.RaftMessage;
import com.sm.charge.raft.server.storage.LogEntry;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:49
 */
public class AppendRequest extends RaftMessage {
    /**
     * 新的日志条目紧随之前的索引值
     */
    private long prevLogIndex;

    /**
     * prevLogIndex 条目的任期号
     */
    private long prevLogTerm;

    /**
     * 领导人已经提交的日志的索引值
     */
    private long leaderCommit;

    /**
     * 准备存储的日志条目（表示心跳时为空；一次性发送多个是为了提高效率）
     */
    private LogEntry[] logEntries;

    public long getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public long getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(long prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    public LogEntry[] getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(LogEntry[] logEntries) {
        this.logEntries = logEntries;
    }
}

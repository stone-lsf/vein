package com.sm.charge.raft.server.replicate;

import com.sm.charge.raft.server.RaftMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:49
 */
public class AppendResponse extends RaftMessage {

    /**
     * 跟随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真
     */
    private boolean success;

    /**
     * 当success=true时，忽略，否则表示follower最后一条日志的下一个index
     */
    private long nextIndex;
}

package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.election.VoteEvent;
import com.sm.charge.raft.server.election.VoteResponse;
import com.sm.charge.raft.server.membership.ConfigurationEvent;
import com.sm.charge.raft.server.membership.ConfigurationResponse;
import com.sm.charge.raft.server.membership.JoinEvent;
import com.sm.charge.raft.server.membership.JoinResponse;
import com.sm.charge.raft.server.membership.LeaveEvent;
import com.sm.charge.raft.server.membership.LeaveResponse;
import com.sm.charge.raft.server.replicate.AppendEvent;
import com.sm.charge.raft.server.replicate.AppendResponse;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:32
 */
public interface ServerState {

    void handle(VoteEvent event);

    void handle(VoteResponse response);

    void handle(JoinEvent event);

    void handle(JoinResponse response);

    void handle(LeaveEvent event);

    void handle(LeaveResponse response);

    void handle(AppendEvent event);

    void handle(AppendResponse response);

    void handle(ConfigurationEvent event);

    void handle(ConfigurationResponse response);
}

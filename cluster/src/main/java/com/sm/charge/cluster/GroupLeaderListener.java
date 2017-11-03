package com.sm.charge.cluster;

import com.sm.charge.cluster.group.LeaderListener;
import com.sm.charge.cluster.group.GroupMembership;
import com.sm.charge.cluster.group.MessageReplicator;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午11:42
 */
public class GroupLeaderListener implements LeaderListener {

    private final MessageReplicator replicator;
    private final GroupMembership membership;

    public GroupLeaderListener(MessageReplicator replicator, GroupMembership membership) {
        this.replicator = replicator;
        this.membership = membership;
    }

    @Override
    public void onLeave(Server leader) {
        membership.joinGroup();
    }

    @Override
    public void onSelected(Server leader) {
        replicator.setLeader(leader);
        replicator.pullMessage();
    }
}

package com.sm.charge.cluster;

import com.sm.charge.cluster.group.GroupMemberService;
import com.sm.charge.cluster.group.LeaderListener;
import com.sm.charge.cluster.group.MessagePullService;
import com.sm.finance.charge.common.base.LoggerSupport;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午11:42
 */
public class GroupLeaderListener extends LoggerSupport implements LeaderListener {

    private final MessagePullService replicator;
    private final GroupMemberService membership;

    public GroupLeaderListener(MessagePullService replicator, GroupMemberService membership) {
        this.replicator = replicator;
        this.membership = membership;
    }

    @Override
    public void onLeave(Server leader) {
        membership.joinGroup();
        try {
            replicator.close();
        } catch (Exception e) {
            logger.error("close replicate caught exception", e);
            throw new RuntimeException("close replicate caught exception", e);
        }
    }

    @Override
    public void onSelected(Server leader) {
        replicator.setLeader(leader);
        replicator.clearMessageFutures();
        try {
            replicator.start();
        } catch (Exception e) {
            logger.error("start replicate caught exception", e);
            throw new RuntimeException("start replicate caught exception", e);
        }
    }
}

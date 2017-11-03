package com.sm.charge.cluster;

import com.sm.charge.cluster.group.LeaderListener;
import com.sm.charge.cluster.group.LeaderSelector;
import com.sm.charge.cluster.group.MessagePuller;
import com.sm.charge.cluster.group.Server;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午11:42
 */
public class GroupLeaderListener implements LeaderListener {

    private final MessagePuller puller;
    private final LeaderSelector selector;

    public GroupLeaderListener(MessagePuller puller, LeaderSelector selector) {
        this.puller = puller;
        this.selector = selector;
    }

    @Override
    public void onLeave(Server leader) {
        selector.joinGroup();
    }

    @Override
    public void onSelected(Server leader) {
        puller.setLeader(leader);
        puller.pullMessage();
    }
}

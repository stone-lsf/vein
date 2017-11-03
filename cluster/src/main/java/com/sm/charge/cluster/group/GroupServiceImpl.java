package com.sm.charge.cluster.group;

import com.sm.charge.cluster.ClusterConfig;
import com.sm.charge.cluster.ClusterMessage;
import com.sm.charge.cluster.GroupLeaderListener;
import com.sm.charge.cluster.Server;
import com.sm.charge.cluster.Store;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午10:39
 */
public class GroupServiceImpl implements GroupService {

    private GroupMembership membership;
    private MessageReplicator replicator;

    public GroupServiceImpl(ServerGroup group, Server self, ClusterConfig config, Store store) {
        this.membership = new GroupMembership(self, group, config.getElectTimeout());
        this.replicator = new MessageReplicator(self, group, config.getMaxAppendSize(), store);
        LeaderListener leaderListener = new GroupLeaderListener(replicator, membership);
        this.membership.setLeaderListener(leaderListener);
        this.replicator.setLeaderListener(leaderListener);
    }


    @Override
    public CompletableFuture<Boolean> receive(ClusterMessage message) {
        return null;
    }
}

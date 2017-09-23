package com.sm.finance.charge.cluster;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:23
 */
public class ChargeCluster implements Cluster {

    @Override
    public ClusterMember master() {
        return null;
    }

    @Override
    public ClusterMember member(long id) {
        return null;
    }

    @Override
    public ClusterMember local() {
        return null;
    }

    @Override
    public int getQuorum() {
        return 0;
    }

    @Override
    public long version() {
        return 0;
    }

    @Override
    public List<ClusterMember> members() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> join() {
        return null;
    }
}

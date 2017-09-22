package com.sm.finance.charge.cluster;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:21
 */
public interface Cluster {

    ClusterMember master();

    ClusterMember member(long id);

    ClusterMember local();

    long version();

    List<ClusterMember> members();

    CompletableFuture<Boolean> join();
}

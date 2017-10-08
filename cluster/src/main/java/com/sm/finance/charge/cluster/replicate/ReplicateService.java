package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.cluster.ClusterMember;
import com.sm.finance.charge.cluster.storage.Entry;
import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.Startable;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:35
 */
public interface ReplicateService extends Startable, Closable {

    CompletableFuture<Object> replicate(Entry entry);

    void replicateTo(ClusterMember member);

    CompletableFuture<ReplicateResponse> handleReplicate(ReplicateRequest request);

    void handleReplicateResponse(ReplicateResponse response, ClusterMember member);

    void handleReplicateResponseFailure(ClusterMember member, ReplicateRequest request, Throwable error);

    void snapshotTo(ClusterMember member);

    CompletableFuture<InstallSnapshotResponse> handleSnapshot(InstallSnapshotRequest request);

    void handleInstallSnapshotResponse(ClusterMember member, InstallSnapshotResponse response, InstallSnapshotRequest request);

    void handleInstallSnapshotResponseFailure(ClusterMember member, InstallSnapshotRequest request, Throwable error);

}

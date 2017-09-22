package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.cluster.ClusterMember;
import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.Startable;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:35
 */
public interface ReplicateService extends Startable, Closable {

    void replicate(ClusterMember member);

    CompletableFuture<ReplicateResponse> handleReplicate(ReplicateRequest request);

    void handleReplicateResponse(ReplicateResponse response);

    void handleReplicateResponseFailure(ClusterMember member, ReplicateRequest request, Throwable error);

    CompletableFuture<InstallSnapshotResponse> install(InstallSnapshotRequest request);

    void handleInstallSnapshotResponse(InstallSnapshotResponse request);

    void handleInstallSnapshotResponseFailure(ClusterMember member, InstallSnapshotRequest request, Throwable error);

}

package com.sm.charge.raft.server.state;

import com.sm.charge.raft.server.RaftState;
import com.sm.charge.raft.server.ServerContext;
import com.sm.charge.raft.server.events.InstallSnapshotRequest;
import com.sm.charge.raft.server.events.InstallSnapshotResponse;
import com.sm.charge.raft.server.events.AppendRequest;
import com.sm.charge.raft.server.events.AppendResponse;
import com.sm.charge.raft.server.storage.snapshot.Snapshot;
import com.sm.charge.raft.server.storage.snapshot.SnapshotManager;
import com.sm.charge.raft.server.storage.snapshot.SnapshotWriter;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 上午12:50
 */
public class PassiveState extends AbstractState {

    private volatile Snapshot pendingSnapshot;
    private volatile long nextSnapshotOffset;
    private final SnapshotManager snapshotManager;

    public PassiveState(ServerContext context) {
        super(context);
        this.snapshotManager = context.getSnapshotManager();
    }

    @Override
    public RaftState state() {
        return RaftState.PASSIVE;
    }


    @Override
    protected AppendResponse doHandle(AppendRequest request) {
        logger.error("passive state com.sm.charge.raft.server should'nt receive append request from master:{}", request.getSource());
        AppendResponse response = new AppendResponse();
        fill(response, request.getSource());
        response.setSuccess(false);
        return response;
    }

    @Override
    public void suspect() {

    }

    @Override
    public void wakeup() {
        logger.info("{} transfer to passive state", self.getNodeId());
    }

    @Override
    public InstallSnapshotResponse handle(InstallSnapshotRequest request) {
        long requestTerm = request.getTerm();
        if (updateTerm(requestTerm)) {
            InstallSnapshotResponse response = new InstallSnapshotResponse();
            fill(response, request.getSource());
            response.setAccepted(false);
            return response;
        }
//        electionTimer.restart();

        if (pendingSnapshot != null && request.getIndex() != pendingSnapshot.index()) {
            pendingSnapshot.close();
            pendingSnapshot.delete();
            pendingSnapshot = null;
            nextSnapshotOffset = 0;
        }

        InstallSnapshotResponse response = new InstallSnapshotResponse();
        fill(response, request.getSource());
        if (pendingSnapshot == null) {
            if (request.getOffset() > 0) {
                response.setAccepted(false);
                response.setNextOffset(0);
                return response;
            }

            pendingSnapshot = snapshotManager.create(request.getIndex(), System.currentTimeMillis());
            nextSnapshotOffset = 0;
        }

        if (request.getOffset() > nextSnapshotOffset) {
            response.setAccepted(false);
            response.setNextOffset(nextSnapshotOffset);
            return response;
        }


        try (SnapshotWriter writer = pendingSnapshot.writer()) {
            writer.position(request.getOffset());
            writer.write(request.getData());
        } catch (Exception e) {
            logger.error("open snapshot:{} writer caught exception", pendingSnapshot.index(), e);
            throw new RuntimeException(e);
        }

        if (request.isComplete()) {
            pendingSnapshot.complete();
            context.getStateMachine().installSnapshot(pendingSnapshot);
            pendingSnapshot = null;
            nextSnapshotOffset = 0;
        } else {
            nextSnapshotOffset += request.getData().length;
        }

        response.setAccepted(true);
        return response;
    }
}

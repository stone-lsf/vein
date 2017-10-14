package com.sm.charge.raft.server;

import com.sm.charge.raft.server.storage.Snapshot;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午12:52
 */
public interface RaftListener {

    void onInstallComplete(Snapshot snapshot);

    void onElectTimeout();

    void onElectAsMaster();

    void onNewLeader();

    void onFallBehind();
}

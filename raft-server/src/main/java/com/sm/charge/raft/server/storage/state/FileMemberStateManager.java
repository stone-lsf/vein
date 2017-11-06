package com.sm.charge.raft.server.storage.state;

import com.sm.charge.raft.server.RaftMember;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/10/14 下午12:22
 */
public class FileMemberStateManager implements MemberStateManager {

    private File file;

    @Override
    public void persistState(RaftMember member) {
    }

    @Override
    public RaftMember loadState() {
        return null;
    }
}

package com.sm.charge.raft.server;

import com.google.common.collect.Lists;

import com.sm.charge.raft.server.election.MasterListener;
import com.sm.finance.charge.common.AbstractService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:23
 */
public class RaftClusterImpl extends AbstractService implements RaftCluster {

    private final String name;
    private final RaftMember self;
    private final ConcurrentMap<Long, RaftMember> members = new ConcurrentHashMap<>();
    private volatile long version;
    private volatile RaftMember master;

    public RaftClusterImpl(String name, RaftMember self) {
        this.name = name;
        this.self = self;
        members.put(self.getId(), self);
    }

    @Override
    public String name() {
        return name;
    }

    public RaftMember master() {
        return master;
    }

    @Override
    public void setMaster(RaftMember master) {
        this.master = master;
    }

    public RaftMember member(long id) {
        return members.get(id);
    }

    public RaftMember local() {
        return self;
    }

    public int getQuorum() {
        return 0;
    }

    public long version() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    public List<RaftMember> members() {
        return Lists.newArrayList(members.values());
    }

    @Override
    public void add(RaftMember member) {
        members.put(member.getId(), member);
    }

    @Override
    public void remove(RaftMemberContext member) {
        members.remove(member.getId());
    }

    @Override
    public void addMemberListener(MemberListener listener) {

    }

    @Override
    public void addMasterListener(MasterListener listener) {

    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {

    }
}

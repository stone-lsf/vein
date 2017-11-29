package com.vein.raft.server;

import com.google.common.collect.Lists;

import com.vein.common.AbstractService;

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
    private final ConcurrentMap<String, RaftMember> members = new ConcurrentHashMap<>();
    private volatile long version;
    private volatile RaftMember master;

    public RaftClusterImpl(String name, RaftMember self) {
        this.name = name;
        this.self = self;
        members.put(self.getNodeId(), self);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public RaftMember master() {
        return master;
    }

    @Override
    public void setMaster(RaftMember master) {
        this.master = master;
    }

    @Override
    public RaftMember member(String memberId) {
        return members.get(memberId);
    }

    @Override
    public boolean contain(String memberId) {
        return false;
    }

    @Override
    public RaftMember local() {
        return self;
    }

    @Override
    public int getQuorum() {
        int size = members.size();
        return (size + 1) / 2;
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public List<RaftMember> members() {
        return Lists.newArrayList(members.values());
    }

    @Override
    public void add(RaftMember member) {
        members.put(member.getNodeId(), member);
    }

    @Override
    public void remove(RaftMember member) {
        members.remove(member.getNodeId());
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

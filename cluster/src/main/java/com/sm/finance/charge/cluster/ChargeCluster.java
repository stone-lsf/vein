package com.sm.finance.charge.cluster;

import com.google.common.collect.Lists;

import com.sm.finance.charge.cluster.elect.MasterListener;
import com.sm.finance.charge.common.AbstractService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/22 上午11:23
 */
public class ChargeCluster extends AbstractService implements Cluster {

    private final String name;
    private final ClusterMember local;
    private final ConcurrentMap<Long, ClusterMember> members = new ConcurrentHashMap<>();
    private volatile long version;
    private volatile ClusterMember master;

    public ChargeCluster(String name, ClusterMember local) {
        this.name = name;
        this.local = local;
    }

    @Override
    public String name() {
        return name;
    }

    public ClusterMember master() {
        return master;
    }

    @Override
    public void setMaster(ClusterMember master) {
        this.master = master;
    }

    public ClusterMember member(long id) {
        return members.get(id);
    }

    public ClusterMember local() {
        return local;
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

    public List<ClusterMember> members() {
        return Lists.newArrayList(members.values());
    }

    @Override
    public void add(ClusterMember member) {
        members.put(member.getId(), member);
    }

    @Override
    public void remove(ClusterMember member) {
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

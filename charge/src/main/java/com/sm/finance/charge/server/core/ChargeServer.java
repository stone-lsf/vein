package com.sm.finance.charge.server.core;

import com.sm.charge.memory.DefaultDiscoveryService;
import com.sm.charge.memory.DiscoveryConfig;
import com.sm.charge.memory.DiscoveryService;
import com.sm.charge.raft.server.RaftConfig;
import com.sm.charge.raft.server.RaftServer;
import com.sm.charge.raft.server.RaftServerImpl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author shifeng.luo
 * @version created on 2017/10/22 上午11:11
 */
@Component
public class ChargeServer implements InitializingBean {

    private RaftServer raftServer;

    private DiscoveryService discoveryService;

    @Override
    public void afterPropertiesSet() throws Exception {
        DiscoveryConfig discoveryConfig = new DiscoveryConfig();
        discoveryService = new DefaultDiscoveryService(discoveryConfig);

        RaftConfig raftConfig = new RaftConfig();
        raftServer = new RaftServerImpl(raftConfig, new PrintLogStateMachine());
    }
}

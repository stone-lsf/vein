package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.discovery.DiscoveryService;

import org.junit.Before;
import org.junit.Test;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午4:34
 */
public class ChargeServerTest {

    private ChargeServer chargeServer;
    private DiscoveryService discoveryService = null;
    private ServerContext serverContext = null;

    @Before
    public void init() {
        this.chargeServer = new ChargeServer(discoveryService, serverContext);
    }

    @Test
    public void join() throws Exception {
    }

    @Test
    public void leave() throws Exception {
    }

    @Test
    public void handle() throws Exception {
    }

}
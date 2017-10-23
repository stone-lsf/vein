package com.sm.charge.memory;

import com.sm.finance.charge.common.base.Configure;
import com.sm.finance.charge.common.base.ConfigureLoader;

import org.junit.Before;
import org.junit.Test;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 下午5:02
 */
public class DiscoveryServiceTest {

    private DiscoveryService service1;
    private DiscoveryService service2;
    private DiscoveryService service3;
    private DiscoveryService service4;
    private DiscoveryService service5;

    @Before
    public void setUp() throws Exception {
        Configure configure = ConfigureLoader.loader("discovery1.properties");
        DiscoveryConfig config1 = new DiscoveryConfig(configure);
        service1 = new DefaultDiscoveryService(config1);

        Configure configure2 = ConfigureLoader.loader("discovery2.properties");
        DiscoveryConfig config2 = new DiscoveryConfig(configure2);
        service2 = new DefaultDiscoveryService(config2);

        Configure configure3 = ConfigureLoader.loader("discovery3.properties");
        DiscoveryConfig config3 = new DiscoveryConfig(configure3);
        service3 = new DefaultDiscoveryService(config3);

        Configure configure4 = ConfigureLoader.loader("discovery4.properties");
        DiscoveryConfig config4 = new DiscoveryConfig(configure4);
        service4 = new DefaultDiscoveryService(config4);

        Configure configure5 = ConfigureLoader.loader("discovery5.properties");
        DiscoveryConfig config5 = new DiscoveryConfig(configure5);
        service5 = new DefaultDiscoveryService(config5);
    }

    @Test
    public void join() throws Exception {
        service1.join("");
        service2.join("");
        service3.join("");
        service4.join("");
        service5.join("");
    }

    @Test
    public void getNodes() throws Exception {
    }

}
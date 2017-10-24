package com.sm.charge.memory;

import com.sm.finance.charge.common.base.Configure;
import com.sm.finance.charge.common.base.ConfigureLoader;
import com.sm.finance.charge.common.utils.ThreadUtil;

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
        Configure configure = ConfigureLoader.loader("test/discovery1.properties");
        DiscoveryConfig config1 = new DiscoveryConfig(configure);
        service1 = new DiscoveryServiceImpl(config1);

        Configure configure2 = ConfigureLoader.loader("test/discovery2.properties");
        DiscoveryConfig config2 = new DiscoveryConfig(configure2);
        service2 = new DiscoveryServiceImpl(config2);

        Configure configure3 = ConfigureLoader.loader("test/discovery3.properties");
        DiscoveryConfig config3 = new DiscoveryConfig(configure3);
        service3 = new DiscoveryServiceImpl(config3);
//
//        Configure configure4 = ConfigureLoader.loader("test/discovery4.properties");
//        DiscoveryConfig config4 = new DiscoveryConfig(configure4);
//        service4 = new DiscoveryServiceImpl(config4);
//
//        Configure configure5 = ConfigureLoader.loader("test/discovery5.properties");
//        DiscoveryConfig config5 = new DiscoveryConfig(configure5);
//        service5 = new DiscoveryServiceImpl(config5);
    }

    @Test
    public void join() throws Exception {
        service1.start();
        service2.start();
        service3.start();
//        service4.start();
//        service5.start();

        boolean success = service1.join("test");
        if (success) {
            System.out.println("node1 join success");
        }
        success = service2.join("test");
        if (success) {
            System.out.println("node2 join success");
        }
        success = service3.join("test");
        if (success) {
            System.out.println("node3 join success");
        }
//        success = service4.join("test");
//        if (success) {
//            System.out.println("node4 join success");
//        }
//        success = service5.join("test");
//        if (success) {
//            System.out.println("node5 join success");
//        }
        int time = 0;
        boolean closed = false;
        while (true) {
            time += 6000;
            if (time > 2 * 60 * 1000 && !closed) {
                System.out.println("close");
                service3.close();
                closed = true;
            } else {
                ThreadUtil.sleep(6000);
            }
        }
    }

    @Test
    public void getNodes() throws Exception {
    }

}
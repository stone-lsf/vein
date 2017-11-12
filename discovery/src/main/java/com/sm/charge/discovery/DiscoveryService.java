package com.sm.charge.discovery;

import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:30
 */
public interface DiscoveryService extends Startable, Closable {

    /**
     * 加入集群
     */
    void join();


    Nodes getNodes();
}

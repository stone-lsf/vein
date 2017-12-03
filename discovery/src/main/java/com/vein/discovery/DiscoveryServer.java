package com.vein.discovery;

import com.vein.common.base.Closable;
import com.vein.common.base.Startable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:30
 */
public interface DiscoveryServer extends Startable, Closable {

    /**
     * 加入集群
     */
    void join();

    /**
     * 优雅离开
     */
    void left();

    Nodes getNodes();
}

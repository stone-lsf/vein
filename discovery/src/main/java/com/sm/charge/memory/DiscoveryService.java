package com.sm.charge.memory;

import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:30
 */
public interface DiscoveryService extends Startable, Closable {

    /**
     * 加入集群
     *
     * @param cluster 集群名称
     * @return 成功则返回true，否则返回false
     */
    boolean join(String cluster);


    DiscoveryNodes getNodes();
}

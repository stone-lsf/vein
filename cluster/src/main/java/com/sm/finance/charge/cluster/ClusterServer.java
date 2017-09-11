package com.sm.finance.charge.cluster;

import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.Startable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午8:28
 */
public interface ClusterServer extends Startable, Closable {

    /**
     * 加入集群
     *
     * @return 成功则返回true，否则返回false
     */
    boolean join();

}

package com.sm.finance.charge.cluster.discovery;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 下午11:03
 */
public interface DiscoveryNodeListener {

    void onJoin(DiscoveryNode node);

    void onUpdate(DiscoveryNode node);

    void onLeave(DiscoveryNode node);
}

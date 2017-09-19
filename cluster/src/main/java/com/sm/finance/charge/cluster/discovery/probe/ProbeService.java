package com.sm.finance.charge.cluster.discovery.probe;

import com.sm.finance.charge.cluster.discovery.DiscoveryNode;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午2:44
 */
public interface ProbeService {

    /**
     * ping节点
     *
     * @param node    目标节点
     * @param timeout 超时时间，单位毫秒
     * @return ack {@link Ack}
     */
    Ack ping(DiscoveryNode node, int timeout);

    /**
     * 间接ping节点
     *
     * @param node    目标节点
     * @param timeout 超时时间，单位毫秒
     * @return 是否间接ping通
     */
    boolean redirectPing(DiscoveryNode node, int timeout);

    Ack handle(Ping ping);


    Ack handle(RedirectPing ping);
}

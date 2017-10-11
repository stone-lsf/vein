package com.sm.charge.memory;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午3:28
 */
public interface NodeFilter {

    /**
     * 判断是否需要过滤节点
     *
     * @param node 节点
     * @return 如果需要过滤，返回true，否则返回false
     */
    boolean apply(DiscoveryNode node);
}

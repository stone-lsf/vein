package com.sm.finance.charge.cluster.discovery.pushpull;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:28
 */
public interface PushPullService {

    /**
     * 跟节点交换信息，push-pull消息
     *
     * @param address 几点地址
     * @throws Exception 异常
     */
    void pushPull(Address address) throws Exception;

    /**
     * 处理push-pull请求
     *
     * @param request push-pull请求
     * @return push-pull结果
     */
    PushPullResponse handle(PushPullRequest request);
}

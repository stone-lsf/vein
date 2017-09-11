package com.sm.finance.charge.cluster.discovery.pushpull;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午10:28
 */
public interface PushPullService {

    void pushPull(Address address) throws Exception;

    PushPullResponse handle(PushPullRequest request);
}

package com.sm.finance.charge.cluster;

import com.sm.finance.charge.cluster.client.Command;

/**
 * @author shifeng.luo
 * @version created on 2017/9/21 下午11:24
 */
public interface StateMachine {

    Object apply(Command command);
}

package com.sm.finance.charge.cluster.discovery.gossip;

import com.sm.finance.charge.cluster.discovery.gossip.messages.AliveMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.DeadMessage;
import com.sm.finance.charge.cluster.discovery.gossip.messages.SuspectMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:23
 */
public class NodeStatusController implements NodeStatusService {

    @Override
    public void aliveNode(AliveMessage message, boolean bootstrap) {

    }

    @Override
    public void suspectNode(SuspectMessage message) {

    }

    @Override
    public void deadNode(DeadMessage message) {

    }
}

package com.sm.finance.charge.cluster.discovery.handler;

import com.sm.finance.charge.cluster.discovery.gossip.GossipMessageService;
import com.sm.finance.charge.cluster.discovery.gossip.GossipRequest;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

/**
 * @author shifeng.luo
 * @version created on 2017/9/19 下午11:43
 */
public class GossipMessagesHandler extends AbstractRequestHandler<GossipRequest> {

    private final GossipMessageService messageService;

    public GossipMessagesHandler(GossipMessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Object handle(GossipRequest request, RequestContext context) throws Exception {
        messageService.handle(request);
        return null;
    }
}

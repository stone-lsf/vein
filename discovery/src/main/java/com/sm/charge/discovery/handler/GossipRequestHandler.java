package com.sm.charge.discovery.handler;

import com.sm.charge.discovery.gossip.GossipMessageService;
import com.sm.charge.discovery.gossip.GossipRequest;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/19 下午11:43
 */
public class GossipRequestHandler extends AbstractRequestHandler<GossipRequest> {

    private final GossipMessageService messageService;

    public GossipRequestHandler(GossipMessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public CompletableFuture<?> handle(GossipRequest request, RequestContext context) throws Exception {
        messageService.handle(request);
        return CompletableFuture.completedFuture(null);
    }
}

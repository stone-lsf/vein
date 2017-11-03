package com.sm.charge.cluster.messages.handlers;

import com.sm.charge.cluster.group.LeaderSelector;
import com.sm.charge.cluster.messages.JoinRequest;
import com.sm.charge.cluster.messages.JoinResponse;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午11:12
 */
public class JoinRequestHandler extends AbstractRequestHandler<JoinRequest> {

    private final LeaderSelector selector;

    public JoinRequestHandler(LeaderSelector selector) {
        this.selector = selector;
    }

    @Override
    public CompletableFuture<JoinResponse> handle(JoinRequest request, RequestContext context) throws Exception {
        return selector.handle(request);
    }
}

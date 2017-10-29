package com.sm.charge.cluster.messages.handlers;

import com.sm.charge.cluster.group.Selector;
import com.sm.charge.cluster.messages.PullRequest;
import com.sm.charge.cluster.messages.PullResponse;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午11:12
 */
public class PullRequestHandler extends AbstractRequestHandler<PullRequest> {

    private final Selector selector;

    public PullRequestHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public CompletableFuture<PullResponse> handle(PullRequest request, RequestContext context) throws Exception {
        return selector.handle(request);
    }
}

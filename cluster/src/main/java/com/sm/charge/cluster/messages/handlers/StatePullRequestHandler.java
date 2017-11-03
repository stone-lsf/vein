package com.sm.charge.cluster.messages.handlers;

import com.sm.charge.cluster.group.LeaderSelector;
import com.sm.charge.cluster.messages.StatePullRequest;
import com.sm.charge.cluster.messages.StatePullResponse;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午11:12
 */
public class StatePullRequestHandler extends AbstractRequestHandler<StatePullRequest> {

    private final LeaderSelector selector;

    public StatePullRequestHandler(LeaderSelector selector) {
        this.selector = selector;
    }

    @Override
    public CompletableFuture<StatePullResponse> handle(StatePullRequest request, RequestContext context) throws Exception {
        return selector.handle(request);
    }
}

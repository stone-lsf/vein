package com.vein.cluster.messages.handlers;

import com.vein.cluster.group.GroupMemberService;
import com.vein.cluster.messages.StatePullRequest;
import com.vein.cluster.messages.StatePullResponse;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午11:12
 */
public class StatePullRequestHandler extends AbstractRequestHandler<StatePullRequest> {

    private final GroupMemberService selector;

    public StatePullRequestHandler(GroupMemberService selector) {
        this.selector = selector;
    }

    @Override
    public CompletableFuture<StatePullResponse> handle(StatePullRequest request, RequestContext context) throws Exception {
        return selector.handle(request);
    }
}

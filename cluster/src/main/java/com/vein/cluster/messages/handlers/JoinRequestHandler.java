package com.vein.cluster.messages.handlers;

import com.vein.cluster.group.GroupMemberService;
import com.vein.cluster.messages.JoinRequest;
import com.vein.cluster.messages.JoinResponse;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午11:12
 */
public class JoinRequestHandler extends AbstractRequestHandler<JoinRequest> {

    private final GroupMemberService selector;

    public JoinRequestHandler(GroupMemberService selector) {
        this.selector = selector;
    }

    @Override
    public CompletableFuture<JoinResponse> handle(JoinRequest request, RequestContext context) throws Exception {
        return selector.handle(request);
    }
}

package com.sm.charge.cluster.messages.handlers;

import com.sm.charge.cluster.group.MessagePuller;
import com.sm.charge.cluster.messages.MessagePullRequest;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午11:40
 */
public class MessagePullRequestHandler extends AbstractRequestHandler<MessagePullRequest> {

    private final MessagePuller puller;

    public MessagePullRequestHandler(MessagePuller puller) {
        this.puller = puller;
    }

    @Override
    public CompletableFuture<?> handle(MessagePullRequest request, RequestContext context) throws Exception {
        return puller.handle(request);
    }
}

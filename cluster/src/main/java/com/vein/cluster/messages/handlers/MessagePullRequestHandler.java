package com.vein.cluster.messages.handlers;

import com.vein.cluster.group.MessagePullService;
import com.vein.cluster.messages.MessagePullRequest;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午11:40
 */
public class MessagePullRequestHandler extends AbstractRequestHandler<MessagePullRequest> {

    private final MessagePullService puller;

    public MessagePullRequestHandler(MessagePullService puller) {
        this.puller = puller;
    }

    @Override
    public CompletableFuture<?> handle(MessagePullRequest request, RequestContext context) throws Exception {
        return puller.handle(request);
    }
}

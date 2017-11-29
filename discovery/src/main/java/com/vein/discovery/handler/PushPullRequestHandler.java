package com.vein.discovery.handler;

import com.vein.discovery.pushpull.PushPullRequest;
import com.vein.discovery.pushpull.PushPullService;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/12 上午12:11
 */
public class PushPullRequestHandler extends AbstractRequestHandler<PushPullRequest> {

    private final PushPullService pushPullService;

    public PushPullRequestHandler(PushPullService pushPullService) {
        this.pushPullService = pushPullService;
    }

    @Override
    public CompletableFuture<?> handle(PushPullRequest message, RequestContext context) throws Exception {
        return pushPullService.handle(message);
    }
}

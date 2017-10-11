package com.sm.charge.memory.handler;

import com.sm.charge.memory.pushpull.PushPullRequest;
import com.sm.charge.memory.pushpull.PushPullService;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

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

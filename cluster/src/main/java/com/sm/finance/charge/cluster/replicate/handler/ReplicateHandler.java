package com.sm.finance.charge.cluster.replicate.handler;

import com.sm.finance.charge.cluster.replicate.ReplicateRequest;
import com.sm.finance.charge.cluster.replicate.ReplicateService;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/23 下午7:08
 */
public class ReplicateHandler extends AbstractRequestHandler<ReplicateRequest> {

    private final ReplicateService service;

    public ReplicateHandler(ReplicateService service) {
        this.service = service;
    }

    @Override
    public CompletableFuture<?> handle(ReplicateRequest request, RequestContext context) throws Exception {
        return service.handleReplicate(request);
    }
}

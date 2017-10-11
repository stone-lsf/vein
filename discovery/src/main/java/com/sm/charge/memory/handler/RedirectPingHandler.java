package com.sm.charge.memory.handler;

import com.sm.charge.memory.probe.ProbeService;
import com.sm.charge.memory.probe.RedirectPing;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/19 下午11:45
 */
public class RedirectPingHandler extends AbstractRequestHandler<RedirectPing> {

    private final ProbeService probeService;

    public RedirectPingHandler(ProbeService probeService) {
        this.probeService = probeService;
    }

    @Override
    public CompletableFuture<?> handle(RedirectPing ping, RequestContext context) throws Exception {
        return probeService.handle(ping);
    }
}

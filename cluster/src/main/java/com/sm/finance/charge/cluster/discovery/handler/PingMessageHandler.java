package com.sm.finance.charge.cluster.discovery.handler;

import com.sm.finance.charge.cluster.discovery.probe.Ping;
import com.sm.finance.charge.cluster.discovery.probe.ProbeService;
import com.sm.finance.charge.transport.api.handler.AbstractRequestHandler;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/19 下午11:40
 */
public class PingMessageHandler extends AbstractRequestHandler<Ping> {

    private final ProbeService probeService;

    public PingMessageHandler(ProbeService probeService) {
        this.probeService = probeService;
    }

    @Override
    public CompletableFuture<?> handle(Ping ping, RequestContext context) throws Exception {
        return probeService.handle(ping);
    }
}

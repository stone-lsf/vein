package com.vein.discovery.handler;

import com.vein.discovery.probe.Ping;
import com.vein.discovery.probe.ProbeService;
import com.vein.transport.api.handler.AbstractRequestHandler;
import com.vein.transport.api.support.RequestContext;

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

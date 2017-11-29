package com.vein.transport.netty;

import com.vein.common.NamedThreadFactory;
import com.vein.transport.api.Transport;
import com.vein.transport.api.TransportClient;
import com.vein.transport.api.TransportServer;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午6:30
 */
public class NettyTransport implements Transport {
    private NioEventLoopGroup workerGroup;
    private int defaultTimeout;

    public NettyTransport(int workerCount, int defaultTimeout) {
        if (workerCount == 0) {
            workerGroup = new NioEventLoopGroup(0, new NamedThreadFactory("NettyPool"));
        } else {
            workerGroup = new NioEventLoopGroup(workerCount, new NamedThreadFactory("NettyPool"));
        }

        this.defaultTimeout = defaultTimeout;
    }


    @Override
    public TransportClient client() {
        return new NettyClient(workerGroup, defaultTimeout);
    }

    @Override
    public TransportServer server() {
        return new NettyServer(workerGroup, defaultTimeout);
    }
}

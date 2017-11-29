package com.vein.transport.netty;

import com.vein.transport.api.Transport;
import com.vein.transport.api.TransportFactory;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午6:35
 */
public class NettyTransportFactory extends TransportFactory {
    @Override
    public Transport doCreate() {
        return new NettyTransport(0, 3000);
    }
}

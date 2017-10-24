package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.AbstractConnection;
import com.sm.finance.charge.transport.api.Request;
import com.sm.finance.charge.transport.api.Response;

import io.netty.channel.Channel;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:47
 */
public class NettyConnection extends AbstractConnection {
    private final Channel channel;

    NettyConnection(Address remoteAddress, Address localAddress, int defaultTimeout, Channel channel) {
        super(remoteAddress, localAddress, defaultTimeout);
        this.channel = channel;
    }

    @Override
    protected void sendRequest(Request request, int timeout) {
        channel.writeAndFlush(request);
        timeoutScheduler.schedule(request.getId(), timeout);
    }

    @Override
    protected void sendRequest(Request request) throws Exception {
        channel.writeAndFlush(request);
    }

    @Override
    protected void sendResponse(Response response) {
        channel.writeAndFlush(response);
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {
        channel.close();
        super.doClose();
    }

    @Override
    public String toString() {
        return "NettyConnection{" +
            "connectionId='" + super.getConnectionId() + '\'' +
            '}';
    }
}

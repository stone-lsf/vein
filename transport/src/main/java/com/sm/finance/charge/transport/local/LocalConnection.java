package com.sm.finance.charge.transport.local;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Request;
import com.sm.finance.charge.transport.api.Response;
import com.sm.finance.charge.transport.api.support.AbstractFutureConnection;
import com.sm.finance.charge.transport.netty.ChannelHelper;

import io.netty.channel.Channel;

/**
 * @author shifeng.luo
 * @version created on 2017/9/23 下午3:45
 */
public class LocalConnection extends AbstractFutureConnection {

    private final Channel channel;
    private final String connectionId;

    public LocalConnection(Address remoteAddress, Address localAddress, int defaultTimeout, Channel channel) {
        super(remoteAddress, localAddress, defaultTimeout);
        this.channel = channel;
        this.connectionId = ChannelHelper.getChannelId(this.channel);
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    protected void sendRequest(Request request, int timeout) {
        channel.writeAndFlush(request);
        timeoutScheduler.schedule(request.getId(), timeout);
    }

    @Override
    protected void sendResponse(Response response) {

    }
}

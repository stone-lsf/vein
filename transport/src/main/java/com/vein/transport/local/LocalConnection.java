package com.vein.transport.local;

import com.vein.common.Address;
import com.vein.transport.api.AbstractConnection;
import com.vein.transport.api.Request;
import com.vein.transport.api.Response;
import com.vein.transport.netty.ChannelHelper;

import io.netty.channel.Channel;

/**
 * @author shifeng.luo
 * @version created on 2017/9/23 下午3:45
 */
public class LocalConnection extends AbstractConnection {

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
    protected void sendAndReceive(Request request, int timeout) {
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
}

package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.AbstractConnection;
import com.sm.finance.charge.transport.api.Request;
import com.sm.finance.charge.transport.api.Response;
import com.sm.finance.charge.transport.api.handler.ResponseHandler;
import com.sm.finance.charge.transport.api.handler.TimeoutResponseHandler;

import io.netty.channel.Channel;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:47
 */
public class NettyConnection extends AbstractConnection {
    private final Channel channel;
    private final String connectionId;

    public NettyConnection(Address remoteAddress, Address localAddress, int defaultTimeout, Channel channel) {
        super(remoteAddress, localAddress, defaultTimeout);
        this.channel = channel;
        this.connectionId = ChannelHelper.getChannelId(this.channel);
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public void send(Object message) throws Exception{
        if (message instanceof Response) {
            channel.writeAndFlush(message);
        } else {
            Request request = new Request(idGenerator.nextId(), message);
            channel.writeAndFlush(request);
        }
    }

    @Override
    protected <T> void sendMessage(Object message, int timeout, ResponseHandler<T> handler) {
        int id = idGenerator.nextId();
        Request request = new Request(id, message);

        TimeoutResponseHandler<T> timeoutHandler;
        if (timeout > 0) {
            timeoutHandler = new TimeoutResponseHandler<>(handler, this, id, timeout);
        } else {
            timeoutHandler = new TimeoutResponseHandler<>(handler, this, id, defaultTimeout);
        }
        responseHandlers.put(id, timeoutHandler);
        channel.writeAndFlush(request);
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
            "connectionId='" + connectionId + '\'' +
            '}';
    }
}

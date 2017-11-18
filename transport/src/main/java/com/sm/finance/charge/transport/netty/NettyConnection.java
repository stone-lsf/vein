package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.AbstractConnection;
import com.sm.finance.charge.transport.api.Request;
import com.sm.finance.charge.transport.api.Response;
import com.sm.finance.charge.transport.api.exceptions.RemoteException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

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
    protected void sendAndReceive(Request request, int timeout) {
        doSend(request);
        timeoutScheduler.schedule(request.getId(), timeout);
    }

    @Override
    protected void sendRequest(Request request) throws Exception {
        doSend(request);
    }

    @Override
    protected void sendResponse(Response response) {
        doSend(response);
    }

    private void doSend(Object obj) {
        boolean success;
        try {
            ChannelFuture future = channel.writeAndFlush(obj);
            success = future.await(defaultTimeout);
        } catch (Throwable e) {
            throw new RemoteException(e);
        }

        if (!success) {
            if (!channel.isOpen()){
                logger.info("connectionId:{} not open",getConnectionId());
            }
            throw new RemoteException("send message timeout:" + defaultTimeout + "ms,connectionId:" + getConnectionId());
        }
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {
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

package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.serializer.protostuff.ProtoStuffSerializer;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.exceptions.ConnectException;
import com.sm.finance.charge.transport.api.support.DefaultConnectionManager;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:31
 */
public class NettyClient extends AbstractService implements TransportClient {

    private final ConnectionManager connectionManager = new DefaultConnectionManager();
    private final Bootstrap bootstrap;
    private final NettyHandler handler;
    private final int defaultTimeout;

    public NettyClient(EventLoopGroup workerGroup, int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
        this.handler = new NettyHandler(connectionManager);

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                    ch.pipeline().addLast("encoder", new LengthFieldPrepender(4, false));
                    ProtoStuffSerializer serialize = new ProtoStuffSerializer(new MessageType());
                    ch.pipeline().addLast("serializer", new SerializerHandler(serialize));
                    ch.pipeline().addLast("handler", handler);
                }
            });
    }

    @Override
    public Connection connect(Address address) throws ConnectException {
        ChannelFuture future;
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(address.getIp(), address.getPort());
            future = bootstrap.connect(socketAddress).sync();
            Channel channel = future.channel();

            InetSocketAddress local = (InetSocketAddress) channel.localAddress();
            Address localAddress = new Address(local);

            NettyConnection connection = new NettyConnection(address, localAddress, defaultTimeout, channel);
            connectionManager.addConnection(connection);
            return connection;
        } catch (Throwable e) {
            logger.error("connect to address:[{}] failed,cased by exception:{}", address, e);
            throw new ConnectException("connect to " + address + " failed", e);
        }
    }

    @Override
    public Connection connect(Address address, int retryTimes) {
        Connection connection = null;

        while (connection == null && retryTimes > 0) {
            try {
                connection = connect(address);
            } catch (ConnectException e) {
                logger.warn("connect to address[{}] failed,remain retry times:{},cased by exception:{}", address, retryTimes, e);
                retryTimes--;
            }
        }
        return connection;
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {

    }

    @Override
    public void close(int timeout) {

    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public Address getLocalAddress() {
        return null;
    }
}

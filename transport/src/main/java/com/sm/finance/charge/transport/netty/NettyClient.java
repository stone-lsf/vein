package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.serializer.protostuff.ProtoStuffSerializer;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionListener;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.exceptions.ConnectException;
import com.sm.finance.charge.transport.api.support.AbstractClient;
import com.sm.finance.charge.transport.api.support.DefaultConnectionManager;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

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
public class NettyClient extends AbstractClient {

    private final ConnectionManager connectionManager = new DefaultConnectionManager();
    private final Bootstrap bootstrap;
    private final NettyHandler handler;
    private final int defaultTimeout;

    NettyClient(EventLoopGroup workerGroup, int defaultTimeout) {
        this.handler = new NettyHandler(connectionManager, defaultTimeout);
        this.defaultTimeout = defaultTimeout;

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
                    ProtoStuffSerializer serialize = new ProtoStuffSerializer(new MessageTypes());
                    ch.pipeline().addLast("serializer", new SerializerHandler(serialize));
                    ch.pipeline().addLast("com.sm.charge.memory.handler", handler);
                }
            });
    }

    @Override
    public CompletableFuture<Connection> connect(Address address) {
        CompletableFuture<Connection> result = new CompletableFuture<>();
        InetSocketAddress socketAddress = new InetSocketAddress(address.getIp(), address.getPort());
        bootstrap.connect(socketAddress).addListener(future -> {
            ChannelFuture channelFuture = (ChannelFuture) future;
            if (channelFuture.isSuccess()) {
                logger.info("connect to:{} success", address);

                Channel channel = channelFuture.channel();
                InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
                Address remoteAddress = new Address(remote);

                InetSocketAddress local = (InetSocketAddress) channel.localAddress();
                Address localAddress = new Address(local);

                NettyConnection connection = new NettyConnection(remoteAddress, localAddress, defaultTimeout, channel);
                connectionManager.addConnection(connection);
                result.complete(connection);
            } else {
                Throwable cause = channelFuture.cause();
                logger.error("connect to address:[{}] failed,cased by exception:{}", address, cause);
                result.completeExceptionally(new ConnectException(cause));
            }
        });

        return result;
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
}

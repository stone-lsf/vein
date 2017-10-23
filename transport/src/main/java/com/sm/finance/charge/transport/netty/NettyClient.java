package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.serializer.protostuff.ProtoStuffSerializer;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionListener;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.exceptions.ConnectException;
import com.sm.finance.charge.transport.api.support.DefaultConnectionManager;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import io.netty.bootstrap.Bootstrap;
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

    NettyClient(EventLoopGroup workerGroup, int defaultTimeout) {
        this.handler = new NettyHandler(connectionManager, new ClientConnectionListener(), defaultTimeout);

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
                logger.info("connect to:{}", address);
                String channelId = ChannelHelper.getChannelId(channelFuture.channel());
                Connection connection = connectionManager.getConnection(channelId);
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
    public CompletableFuture<Connection> connect(Address address, int retryTimes) {
        CompletableFuture<Connection> result = new CompletableFuture<>();

        connect(address).whenComplete((connection, error) -> {
            if (error != null) {
                logger.warn("connect to address:[{}] caught exception:{}", address, error);
                if (retryTimes > 0) {
                    connect(address, retryTimes - 1).whenComplete((conn, e) -> {
                        if (e == null) {
                            result.complete(conn);
                        } else {
                            result.completeExceptionally(e);
                        }
                    });
                } else {
                    result.complete(null);
                }
            } else {
                result.complete(connection);
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

    private class ClientConnectionListener implements ConnectionListener {

        @Override
        public void onConnect(Connection connection) {
        }
    }
}

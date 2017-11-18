package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.utils.AddressUtil;
import com.sm.finance.charge.serializer.protostuff.ProtoStuffSerializer;
import com.sm.finance.charge.transport.api.ConnectionListener;
import com.sm.finance.charge.transport.api.ConnectionManager;
import com.sm.finance.charge.transport.api.TransportServer;
import com.sm.finance.charge.transport.api.exceptions.BindException;
import com.sm.finance.charge.transport.api.support.DefaultConnectionManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午6:27
 */
public class NettyServer extends AbstractService implements TransportServer {
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup workerGroup;
    private final int defaultTimeout;
    private final ConnectionManager connectionManager = new DefaultConnectionManager();
    private Address bindAddress;

    public NettyServer(EventLoopGroup workerGroup, int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
        this.workerGroup = workerGroup;
        this.bootstrap = new ServerBootstrap();
    }


    @Override
    public void listen(int port, ConnectionListener listener) throws BindException {
        bootstrap.group(new NioEventLoopGroup(1), workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    ch.pipeline().addLast("encoder", new LengthFieldPrepender(4, false));
                    ProtoStuffSerializer serialize = new ProtoStuffSerializer(new MessageTypes());
                    ch.pipeline().addLast("serializer", new SerializerHandler(serialize));
                    ch.pipeline().addLast("handler", new NettyHandler(connectionManager, listener, defaultTimeout));
                }
            });
        try {
            this.bindAddress = AddressUtil.getLocalAddress(port);
            bootstrap.bind(bindAddress.getIp(), bindAddress.getPort()).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("Started hetu server on port: " + bindAddress.getPort());
                } else {
                    logger.error("Failed to bind hetu server on port: " + bindAddress.getPort(), future.cause());
                    throw new Exception("Failed to start hetu server on port: " + bindAddress.getPort());
                }
            }).sync();

            logger.info("bind port:[{}] success!", port);
        } catch (InterruptedException e) {
            throw new BindException("bind port " + port + " failed", e);
        }
    }

    @Override
    public Address getBindAddress() {
        return bindAddress;
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {
        connectionManager.closeAll();
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public void close(int timeout) throws Exception {
        connectionManager.closeAll();
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

}

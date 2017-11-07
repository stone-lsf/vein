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
            .option(ChannelOption.SO_BACKLOG, 100)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                    ch.pipeline().addLast("encoder", new LengthFieldPrepender(4, false));
                    ProtoStuffSerializer serialize = new ProtoStuffSerializer(new MessageTypes());
                    ch.pipeline().addLast("serializer", new SerializerHandler(serialize));
                    ch.pipeline().addLast("com.sm.charge.memory.handler", new NettyHandler(connectionManager, listener, defaultTimeout));
                }
            });
        try {
            this.bindAddress = AddressUtil.getLocalAddress(port);
            bootstrap.bind(bindAddress.getIp(), bindAddress.getPort()).sync();
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

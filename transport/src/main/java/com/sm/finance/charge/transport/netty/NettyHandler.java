package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.ConnectionListener;
import com.sm.finance.charge.transport.api.ConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:45
 */
@Sharable
public class NettyHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyHandler.class);
    private final ConnectionManager connectionManager;
    private ConnectionListener listener;
    private final int defaultTimeout;

    /**
     * NettyHandler 构造函数，创建Client时调用
     *
     * @param connectionManager 连接管理器
     */
    public NettyHandler(ConnectionManager connectionManager, int defaultTimeout) {
        this.connectionManager = connectionManager;
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * NettyHandler 构造函数，
     *
     * @param connectionManager 连接管理器
     * @param listener          连接监听器
     * @param defaultTimeout    连接超时时间
     */
    public NettyHandler(ConnectionManager connectionManager, ConnectionListener listener, int defaultTimeout) {
        this.connectionManager = connectionManager;
        this.listener = listener;
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.info("receive channel:{}", channel);
        if (listener != null) {
            receiveChannel(channel);
        }
        super.channelActive(ctx);
    }

    /**
     * 服务端接收到客户端连接
     *
     * @param channel channel
     */
    private void receiveChannel(Channel channel) {
        InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
        Address remoteAddress = new Address(remote);

        InetSocketAddress local = (InetSocketAddress) channel.localAddress();
        Address localAddress = new Address(local);

        NettyConnection connection = new NettyConnection(remoteAddress, localAddress, defaultTimeout, channel);
        logger.info("connection id:{}", connection.getConnectionId());
        connectionManager.addConnection(connection);
        listener.onConnect(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Connection connection = connectionManager.getConnection(ChannelHelper.getChannelId(ctx.channel()));
        if (connection != null) {
            logger.debug("connection:", connection);
            connection.onMessage(msg);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeConnection(ChannelHelper.getChannelId(ctx.channel()));
        if (connection != null) {
            connection.close();
        }
        super.channelInactive(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("caught exception:", cause);
        super.exceptionCaught(ctx, cause);
    }
}

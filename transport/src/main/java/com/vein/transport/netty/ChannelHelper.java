package com.vein.transport.netty;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:39
 */
public class ChannelHelper {

    public static String getChannelId(Channel channel) {
        InetSocketAddress local = (InetSocketAddress) channel.localAddress();
        InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();

        return local.getHostString() + ":" + local.getPort() + "/" + remote.getHostString() + ":" + remote.getPort();
    }
}

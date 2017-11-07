package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.serializer.api.Serializable;
import com.sm.finance.charge.serializer.api.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:44
 */
public class SerializerHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SerializerHandler.class);
    private final Serializer serializer;

    public SerializerHandler(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Serializable)) {
            logger.error("want to transport not serializable class:{} object", msg.getClass());
            throw new IllegalArgumentException("not serializable class:"+msg.getClass());
        }
        Serializable serializable = (Serializable) msg;
        byte[] bytes = serializer.serialize(serializable);
        ByteBuf bb = ctx.alloc().buffer(bytes.length);
        bb.writeBytes(bytes);
        ctx.write(bb, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] dst = new byte[((ByteBuf) msg).readableBytes()];
        buf.readBytes(dst);

        Object dest = serializer.deserialize(dst);
        super.channelRead(ctx, dest);
    }
}

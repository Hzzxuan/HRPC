package com.hzzx.channelHandler;

import com.hzzx.channelHandler.inboundHandler.MysimpleTest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author : HuangZx
 * @date : 2024/5/30 21:36
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new MysimpleTest());
    }
}

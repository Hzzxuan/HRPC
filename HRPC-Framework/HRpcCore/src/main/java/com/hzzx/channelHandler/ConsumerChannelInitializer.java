package com.hzzx.channelHandler;

import com.hzzx.channelHandler.inboundHandler.CallBackHandler;
import com.hzzx.channelHandler.inboundHandler.MysimpleTest;
import com.hzzx.channelHandler.inboundHandler.ResponseMessageDecoder;
import com.hzzx.channelHandler.outboundHandler.RequestMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author : HuangZx
 * @date : 2024/5/30 21:36
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new RequestMessageEncoder())
                .addLast(new ResponseMessageDecoder())
                .addLast(new CallBackHandler());

    }
}

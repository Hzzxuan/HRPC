package com.hzzx.channelHandler.inboundHandler;

import com.hzzx.HBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @author : HuangZx
 * @date : 2024/5/30 21:41
 */
@Slf4j
public class MysimpleTest extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info("msg:{}",msg.toString(Charset.defaultCharset()));
        CompletableFuture<Object> completableFuture = HBootstrap.PENDING_FUTURE.get(1L);
        completableFuture.complete(msg.toString(Charset.defaultCharset()));
    }
}

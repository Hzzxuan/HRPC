package com.hzzx;
import com.hzzx.channelHandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @author : HuangZx
 * @date : 2024/5/30 16:57
 */

/**
 * 在客户端发送命令时总需要初始化bootstrap进行配置，对bootstrap设计单例模式
 */
@Slf4j
public class BootstrapInitializer {
    private static Bootstrap bootstrap = new Bootstrap();
    private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    static {
        //服务调用端启动辅助类
        bootstrap = bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    public BootstrapInitializer(){

    }

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }

}

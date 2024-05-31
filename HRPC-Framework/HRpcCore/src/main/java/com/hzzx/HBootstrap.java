package com.hzzx;


import com.hzzx.channelHandler.inboundHandler.MessageDecoder;
import com.hzzx.discovery.RegistryConfig;
import com.hzzx.utils.ZookeeperNode;
import com.hzzx.utils.ZookeeperUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

/**
 * @author : HuangZx
 * @date : 2024/5/28 15:24
 */
@Slf4j
public class HBootstrap {
    private static final HBootstrap hBootstrap = new HBootstrap();
    private String applicationName;
    private RegistryConfig registryConfig;
    private ProtocalConfig protocalConfig;

    private static final Map<String,ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    public static final Map<Long, CompletableFuture<Object>> PENDING_FUTURE = new ConcurrentHashMap<>(128);
    /**
     * 得到单例对象
     * @return
     */
    public static HBootstrap getInstance() {
        return hBootstrap;
    }

    /**
     * 定义当前应用的Id，将来可能传入注册中心
     * @return
     */
    public HBootstrap application(String name) {
        this.applicationName = name;
        return this;
    }

    /**
     * 连接注册中心
     * @return
     */
    public HBootstrap registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 配置序列化协议
     * @param protocalConfig
     * @return
     */
    public HBootstrap protocal(ProtocalConfig protocalConfig) {
        this.protocalConfig = protocalConfig;
        return this;
    }

    /**
     * 向注册中心注册服务
     * @return
     */
    public HBootstrap publish(ServiceConfig<?> service) {
        //获取注册中心信息并注册服务
        registryConfig.getRegistry().registry(service);
        //维护映射关系 方法名<--->服务
        SERVICE_LIST.put(service.getInterface().getName(),service);
        return this;
    }

    /**
     * 批量向注册中心注册服务
     * @param serviceList
     * @return
     */
    public HBootstrap publish(List<ServiceConfig> serviceList) {
        return this;
    }
    public void start() {
        //服务端开启netty进行通信
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);
        try {
            //服务端启动辅助类
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap = serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
                                    //.addLast(new MessageDecoder())
                                    .addLast(new SimpleChannelInboundHandler<>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    log.info("byteBuf-->{}",byteBuf.toString(StandardCharsets.UTF_8));
                                    ctx.channel().writeAndFlush(Unpooled.copiedBuffer("hello I am server".getBytes()));
                                }
                            });
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(8099).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                bossGroup.shutdownGracefully().sync();
                workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     ****************************请求端api*****************************
     */

    public void reference(ReferenceConfig<?> reference) {
        reference.setRegistryConfig(registryConfig);
    }
}

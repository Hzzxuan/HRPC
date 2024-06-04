package com.hzzx;


import com.hzzx.channelHandler.inboundHandler.RequestMessageDecoder;
import com.hzzx.channelHandler.inboundHandler.MethodCallHandler;
import com.hzzx.channelHandler.outboundHandler.ResponseMessageEncoder;
import com.hzzx.discovery.RegistryConfig;
import com.hzzx.loadbalance.Impl.RoundRobinLoadBalancer;
import com.hzzx.loadbalance.LoadBalancer;
import com.hzzx.message.RpcRequest;
import com.hzzx.utils.IdGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
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
    public static ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    private LoadBalancer loadBalancer;

    public static final int port = 8101;
    public static final IdGenerator ID_GENERATOR= new IdGenerator(0,1);

    public static final Map<String,ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    public static final Map<String,SortedMap<Long,Channel>> ANSWER_TIME_CHANNEL_CACHE = new HashMap<>();
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
        //todo:写这里不合适后续调整
        this.loadBalancer= (LoadBalancer) new RoundRobinLoadBalancer(this.registryConfig.getRegistry());
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
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new RequestMessageDecoder())
                                    .addLast(new MethodCallHandler())
                                    .addLast(new ResponseMessageEncoder());

                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(HBootstrap.port).sync();
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

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }
    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

}

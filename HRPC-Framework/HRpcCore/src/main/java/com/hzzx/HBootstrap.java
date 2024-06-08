package com.hzzx;


import com.hzzx.annotation.HRpcSupply;
import com.hzzx.channelHandler.inboundHandler.RequestMessageDecoder;
import com.hzzx.channelHandler.inboundHandler.MethodCallHandler;
import com.hzzx.channelHandler.outboundHandler.ResponseMessageEncoder;
import com.hzzx.config.Configuration;
import com.hzzx.discovery.RegistryConfig;
import com.hzzx.loadbalance.Impl.RoundRobinLoadBalancer;
import com.hzzx.loadbalance.LoadBalancer;
import com.hzzx.message.RpcRequest;
import com.hzzx.protection.RateLimiter;
import com.hzzx.utils.IdGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * @author : HuangZx
 * @date : 2024/5/28 15:24
 */
@Slf4j
public class HBootstrap {
    private static final HBootstrap hBootstrap = new HBootstrap();

    private final Configuration configuration;
    public Configuration getConfiguration() {
        return configuration;
    }

    public static ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();



    public static final Map<String,ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    public static final Map<String,SortedMap<Long,Channel>> ANSWER_TIME_CHANNEL_CACHE = new HashMap<>();
    public static final Map<Long, CompletableFuture<Object>> PENDING_FUTURE = new ConcurrentHashMap<>(128);

    //构造方法private
    private HBootstrap(){
        this.configuration = new Configuration();
    }
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
        configuration.setApplicationName(name);
        return this;
    }

    /**
     * 连接注册中心
     * @return
     */
    public HBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        //todo:写这里不合适后续调整
        //this.loadBalancer= (LoadBalancer) new RoundRobinLoadBalancer(this.registryConfig.getRegistry());
        return this;
    }

    /**
     * 配置序列化的方式
     * @param serializeType 序列化的方式
     */
    public HBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()) {
            log.debug("我们配置了使用的序列化的方式为【{}】.", serializeType);
        }
        return this;
    }

    public HBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()) {
            log.debug("我们配置了使用的压缩算法为【{}】.", compressType);
        }
        return this;
    }

    public HBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }
    /**
     * 向注册中心注册服务
     * @return
     */
    public  HBootstrap publish(ServiceConfig<?> service) {
        //获取注册中心信息并注册服务
        configuration.getRegistryConfig().getRegistry().registry(service);
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
        serviceList.forEach(service->{
            //获取注册中心信息并注册服务
            configuration.getRegistryConfig().getRegistry().registry(service);
            //维护映射关系 方法名<--->服务
            SERVICE_LIST.put(service.getInterface().getName(),service);
        });
        return this;
    }

    public HBootstrap scan(String packageName){
        // 1、需要通过packageName获取其下的所有的类的权限定名称
        List<String> allClassNames = getAllClassNames(packageName);
        //通过反射获得接口，构建具体实现
        List<Class<?>> classList = allClassNames.stream().map(classname -> {
                    try {
                        return Class.forName(classname);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(HRpcSupply.class) != null)
                .collect(Collectors.toList());
        for(Class<?> clazz : classList){
            //一个类可能实现了多个接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            for(Class<?> ifc : interfaces){
                //发布
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(ifc);
                serviceConfig.setRef(instance);
                if (log.isDebugEnabled()){
                    log.debug("--------通过包扫描，将服务【{}】发布.",ifc);
                }
                publish(serviceConfig);
            }

        }
        return this;
    }

    public static List<String> getAllClassNames(String packageName) {
        String basePath = packageName.replaceAll("\\.","/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(url == null){
            log.error("包扫描时找不到包路径");
            throw  new RuntimeException();
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        recursionFile(absolutePath,classNames,basePath);
        return classNames;
    }

    private static List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        File file = new File(absolutePath);
        if(file.isFile()){
            // 文件 --> 类的权限定名称
            String className = getClassNameByAbsolutePath(absolutePath,basePath);
            classNames.add(className);
            return classNames;
        }else if(file.isDirectory()){
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null || children.length == 0){
                return classNames;
            }
            for (File child : children){
                recursionFile(child.getAbsolutePath(),classNames,basePath);
            }
        }
        return classNames;
    }

    private static String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        return fileName;
    }


    public static void main(String[] args) {
        String bashPath = "com/hzzx";
        String abPath = ClassLoader.getSystemClassLoader().getResource(bashPath).getPath();
        List<String> list = new ArrayList<>();
        System.out.println(recursionFile(abPath,list,bashPath));

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
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
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
        reference.setRegistryConfig(configuration.getRegistryConfig());
    }

    public LoadBalancer getLoadBalancer() {
        return configuration.getLoadBalancer();
    }
    public RegistryConfig getRegistryConfig() {
        return configuration.getRegistryConfig();
    }

}

package com.hzzx.proxy;

import com.hzzx.BootstrapInitializer;
import com.hzzx.HBootstrap;
import com.hzzx.compress.CompressFactory;
import com.hzzx.discovery.Registry;
import com.hzzx.discovery.RegistryConfig;
import com.hzzx.enumeration.RequestType;
import com.hzzx.exceptions.NetworkException;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcRequest;
import com.hzzx.serialize.SerializerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author : HuangZx
 * @date : 2024/5/30 21:18
 */
@Slf4j
public class ConsumerInvocationHandler implements InvocationHandler {
    private RegistryConfig registryConfig;
    private Class<?> interfaceRef;

    public ConsumerInvocationHandler(RegistryConfig registryConfig,Class<?> interfaceRef){
        this.registryConfig = registryConfig;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /**-------------------------封装请求报文-------------------------*/
        RequestLoad requestLoad = RequestLoad.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();
        long randomId = HBootstrap.getInstance().getConfiguration().getID_GENERATOR().getId();
        RpcRequest rpcRequest = RpcRequest.builder().requestId(randomId)
                .compressType(CompressFactory.getCompressorWrapper("gzip").getCode())
                //后续将"jdk"写在配置类中，
                .serializeType(SerializerFactory.getSerializerWrapper("hessian").getCode())
                .requestType(RequestType.REQUEST.getId())
                //.timeStamp(System.currentTimeMillis())
                .requestLoad(requestLoad)
                .build();

        HBootstrap.REQUEST_THREAD_LOCAL.set(rpcRequest);

        //consumer端需要去注册中心找到需要的服务。传入的是method和args
        Registry registry = registryConfig.getRegistry();
        //查找注册中心，得到可用节点，返回ip+端口
        //todo:实现负载均衡策略，
        InetSocketAddress address = HBootstrap.getInstance().getLoadBalancer().chooseServiceAddress(interfaceRef.getName());
        //InetSocketAddress address = registry.lookup(interfaceRef.getName()).get(0);
        if(log.isDebugEnabled()){
            log.info("服务调用方，发现了服务【{}】可用主机{}",interfaceRef.getName(),address);
        }
        //启动客户端netty服务,得到一条channel
        Channel channel = getAvailableChannel(address);



        /**-------------------------同步策略-------------------------
         ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
         if(channelFuture.isDone()){
         Object now = channelFuture.getNow();
         }else if(!channelFuture.isSuccess()){
         Throwable cause = channelFuture.cause();
         throw new RuntimeException(cause)；
         }*/
        /**-------------------------异步策略-------------------------**/
        //将报文write出去
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        channel.writeAndFlush(rpcRequest).addListener(
                (ChannelFutureListener) promise ->{
                            /*if(promise.isDone()){
                                completableFuture.complete(promise.getNow());
                            }else */
                    if(!promise.isSuccess()){
                        completableFuture.completeExceptionally(promise.cause());
                    }
                }
        );
        //writeAndFlush方法发送后，直接调用complete，future得到的只是发出去的动作，需要得到对端的通知才能complete
        //将completableFuture暴露出去，等待对端发送回结果通知Future，在handler结束时执行。
        HBootstrap.PENDING_FUTURE.put(randomId,completableFuture);

        return completableFuture.get(3, TimeUnit.SECONDS);
    }

    private Channel getAvailableChannel(InetSocketAddress address){
        //每次调用invoke方法需要从缓存中读取channel，不能每次调用都新建长连接。
        Channel channel = HBootstrap.CHANNEL_CACHE.get(address);
        if(channel == null){
            //Bootstrap bootstrap = BootstrapInitializer.getBootstrap();
            //channel = bootstrap.connect(address).sync().channel();
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            BootstrapInitializer.getBootstrap().connect(address).addListener(
                    (ChannelFutureListener) promise->{
                        if (promise.isDone()){
                            channelFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    }
            );
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
            HBootstrap.CHANNEL_CACHE.put(address,channel);
        }
        if(channel == null){
            throw new NetworkException("获取通道失败");
        }
        return channel;
    }
}

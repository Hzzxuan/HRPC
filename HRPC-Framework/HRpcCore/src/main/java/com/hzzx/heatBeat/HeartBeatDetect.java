package com.hzzx.heatBeat;

import com.hzzx.BootstrapInitializer;
import com.hzzx.HBootstrap;
import com.hzzx.compress.CompressFactory;
import com.hzzx.discovery.Registry;
import com.hzzx.enumeration.RequestType;
import com.hzzx.message.RpcRequest;
import com.hzzx.serialize.SerializerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author : HuangZx
 * @date : 2024/6/3 23:12
 */
@Slf4j
public class HeartBeatDetect {
    //在拉取服务列表后，需要对建立的channel发送心跳检测包,心跳检测应该在调用端执行，且放在执行函数后
    //将建立的心跳请求连接加入全局channel缓存，在此类中维护一个临时的缓存channelMap。
    public static void start(String serviceName){
        Registry registry = HBootstrap.getInstance().getRegistryConfig().getRegistry();
        List<InetSocketAddress> addressList = registry.lookup(serviceName);
        Map<InetSocketAddress,Channel> channelMap = new ConcurrentHashMap<>(16);
        addressList.forEach(address->{
            Channel channel = null;
            if (!HBootstrap.CHANNEL_CACHE.containsKey(address)) {
                try {
                    channel = BootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    channelMap.put(address,channel);
                    HBootstrap.CHANNEL_CACHE.put(address,channel);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }else {
                channelMap.put(address,HBootstrap.CHANNEL_CACHE.get(address));
            }

        });

        Thread thread =  new Thread(()->{
            new Timer().scheduleAtFixedRate(new HeartBeatSend(channelMap,serviceName),0,2000);
        });
        thread.setDaemon(true);
        thread.start();
    }


    private static class HeartBeatSend extends TimerTask{
        private Map<InetSocketAddress,Channel> channelMap;
        private String serviceName;
        private SortedMap<Long,Channel> timeMap = new TreeMap<>();


        public HeartBeatSend(Map<InetSocketAddress,Channel> channelMap,String serviceName) {
            this.channelMap = channelMap;
            this.serviceName = serviceName;
        }

        @Override
        public void run() {
            timeMap.clear();
            for(Map.Entry<InetSocketAddress, Channel> entry : channelMap.entrySet()){
                //如果全局缓存中没有此服务列表，直接跳过
                if(!HBootstrap.CHANNEL_CACHE.containsKey(entry.getKey())){
                    continue;
                }
                Channel channel = entry.getValue();
                //定义重试次数
                int tryTimes = 3;
                while (tryTimes > 0){
                    //构建心跳检测报文
                    long randomId = HBootstrap.ID_GENERATOR.getId();
                    RpcRequest rpcRequest = RpcRequest.builder().requestId(randomId)
                            .compressType(CompressFactory.getCompressorWrapper("gzip").getCode())
                            //后续将"jdk"写在配置类中，
                            .serializeType(SerializerFactory.getSerializerWrapper("hessian").getCode())
                            .requestType(RequestType.HEART_BEAT.getId())
                            //.timeStamp(System.currentTimeMillis())
                            .build();
                    /**---------------------------------------异步写报文------------------------------------**/
                    long startTime = System.currentTimeMillis();
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    channel.writeAndFlush(rpcRequest).addListener(
                            (ChannelFutureListener) promise ->{
                                if(!promise.isSuccess()){
                                    completableFuture.completeExceptionally(promise.cause());
                                }
                            }
                    );
                    //writeAndFlush方法发送后，直接调用complete，future得到的只是发出去的动作，需要得到对端的通知才能complete
                    //将completableFuture暴露出去，等待对端发送回结果通知Future，在handler结束时执行。
                    HBootstrap.PENDING_FUTURE.put(randomId,completableFuture);
                    Long endTime = 0L;
                    try {
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        tryTimes--;
                        log.error("和地址为【{}】的主机连接发生异常.正在进行第【{}】次重试......",
                                channel.remoteAddress(), 3 - tryTimes);
                        if(tryTimes == 0){
                            //不能在遍历的过程中删除正在遍历的Map,删全局的
                            //channelMap.remove(entry.getKey());
                            HBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                            log.error("和地址为【{}】的主机连接失败......",
                                    channel.remoteAddress());
                        }
                        try {
                            Thread.sleep(10*(new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    Long totalTime = endTime - startTime;
                    //
                    timeMap.put(totalTime,channel);
                    log.debug("和[{}]服务器的响应时间是[{}].",entry.getKey() , totalTime);
                    break;
                }
            }
            HBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(serviceName,timeMap);

        }
    }
}

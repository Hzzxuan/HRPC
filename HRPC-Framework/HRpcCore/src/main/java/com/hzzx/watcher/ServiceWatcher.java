package com.hzzx.watcher;

import com.hzzx.BootstrapInitializer;
import com.hzzx.HBootstrap;
import com.hzzx.discovery.Registry;
import com.hzzx.loadbalance.LoadBalancer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author : HuangZx
 * @date : 2024/6/4 21:44
 */
@Slf4j
public class ServiceWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("检测到服务【{}】下有节点上/下线，将重新拉取服务列表...", event.getPath());
        }
        String serviceName = getServiceName(event.getPath());
        Registry registry = HBootstrap.getInstance().getRegistryConfig().getRegistry();
        //新增/删除一个节点，要更改cache
        List<InetSocketAddress> addresses = registry.lookup(serviceName);
        for(InetSocketAddress address : addresses){
            if(!HBootstrap.CHANNEL_CACHE.containsKey(address)){
                //get一个channel，增加到缓存
                Channel channel = null;
                try {
                    BootstrapInitializer.getBootstrap().connect(address).sync().channel();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                HBootstrap.CHANNEL_CACHE.put(address,channel);
            }
        }
        //一个节点下线，新拉取的Address中不存在，而缓存中存在，遍历缓存keyset，如果发现新的Addresses中没有，从缓存中删了
        for(InetSocketAddress address : HBootstrap.CHANNEL_CACHE.keySet()){
            if(!addresses.contains(address)){
                HBootstrap.CHANNEL_CACHE.remove(address);
            }
        }
        //处理完缓存，重新做负载均衡
        LoadBalancer loadBalancer = HBootstrap.getInstance().getLoadBalancer();
        loadBalancer.rebalance(serviceName,addresses);

    }
    private String getServiceName(String path){
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}



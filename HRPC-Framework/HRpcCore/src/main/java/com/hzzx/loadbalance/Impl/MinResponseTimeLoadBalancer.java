package com.hzzx.loadbalance.Impl;

import com.hzzx.HBootstrap;
import com.hzzx.discovery.Registry;
import com.hzzx.loadbalance.AbstractLoadBalancer;
import com.hzzx.loadbalance.LoadBalancer;
import com.hzzx.loadbalance.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : HuangZx
 * @date : 2024/6/4 16:00
 */
@Slf4j
public class MinResponseTimeLoadBalancer implements LoadBalancer {
    //维护的是Map<ServiceName,SortedMap>
    private Registry registry;


    //todo:registry全局配置
    public MinResponseTimeLoadBalancer(Registry registry){
        this.registry = registry;
    }
    @Override
    public InetSocketAddress chooseServiceAddress(String serviceName){
        SortedMap<Long, Channel> channelSortedMap = HBootstrap.ANSWER_TIME_CHANNEL_CACHE.get(serviceName);
        if(!channelSortedMap.isEmpty()){
            log.debug("返回一最小响应结果");
            return (InetSocketAddress) channelSortedMap.get(channelSortedMap.firstKey()).remoteAddress();
        }
        else {
            log.debug("无最小响应结果，返回默认");
            List<InetSocketAddress> serviceList = registry.lookup(serviceName);
            if(serviceList.isEmpty() || serviceList == null){
                log.error("无可用服务节点");
                //todo 自定义exception表示
                throw new RuntimeException();
            }
            return serviceList.get(0);
        }
    }

}

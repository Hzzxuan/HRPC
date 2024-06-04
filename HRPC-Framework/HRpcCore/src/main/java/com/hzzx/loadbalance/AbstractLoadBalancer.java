package com.hzzx.loadbalance;

import com.hzzx.discovery.Registry;
import com.hzzx.loadbalance.Impl.RoundRobinLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : HuangZx
 * @date : 2024/6/3 10:27
 */
public abstract class AbstractLoadBalancer implements LoadBalancer{
    private Map<String,Selector> selectorCache = new ConcurrentHashMap<>(8);
    private Registry registry;


    //todo:registry全局配置
    public AbstractLoadBalancer(Registry registry){
        this.registry = registry;
    }
    public InetSocketAddress chooseServiceAddress(String serviceName) {
        List<InetSocketAddress> serviceList = registry.lookup(serviceName);
        //查询所有可用节点
        Selector selector = selectorCache.get(serviceName);
        if(selector == null){
            selector = getSelector(serviceList);
            selectorCache.put(serviceName,selector);
        }
        InetSocketAddress target = selector.getNext();
        return target;
    }

    @Override
    public void rebalance(String serviceName, List<InetSocketAddress> addresses) {
        selectorCache.put(serviceName,getSelector(addresses));
    }

    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);
}

package com.hzzx.loadbalance.Impl;

import com.hzzx.discovery.Registry;
import com.hzzx.loadbalance.AbstractLoadBalancer;
import com.hzzx.loadbalance.LoadBalancer;
import com.hzzx.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : HuangZx
 * @date : 2024/6/4 16:00
 */
@Slf4j
public class MinResponseTimeLoadBalancer extends AbstractLoadBalancer {

    public MinResponseTimeLoadBalancer(Registry registry) {
        super(registry);
    }

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return null;
    }

    private static class RoundRobinSelector implements Selector{
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index = new AtomicInteger(0);
        public RoundRobinSelector(List<InetSocketAddress> serviceList){
            this.serviceList = serviceList;
        }
        @Override
        public InetSocketAddress getNext() {
            if(serviceList.isEmpty() || serviceList == null){
                log.error("无可用服务节点");
                //todo 自定义exception表示
                throw new RuntimeException();
            }
            int i = index.get();
            if(i == serviceList.size()-1){
                index.set(0);
            }
            index.incrementAndGet();
            return serviceList.get(i);
        }

        @Override
        public void renewBalance() {

        }
    }
}

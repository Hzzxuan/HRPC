package com.hzzx.loadbalance.Impl;

import com.hzzx.HBootstrap;
import com.hzzx.discovery.Registry;
import com.hzzx.loadbalance.AbstractLoadBalancer;
import com.hzzx.loadbalance.LoadBalancer;
import com.hzzx.loadbalance.Selector;
import com.hzzx.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : HuangZx
 * @date : 2024/6/3 11:40
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {
    public ConsistentHashLoadBalancer(Registry registry) {
        super(registry);
    }

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return null;
    }

    private static class ConsistendHashSelector implements Selector{
        //用hash环存储服务器节点
        private SortedMap<Integer,InetSocketAddress> hashCircle = new TreeMap<>();
        private int virtueNodes;
        private List<InetSocketAddress> serviceList;

        public ConsistendHashSelector(List<InetSocketAddress> serviceList,int virtueNodes){
            this.serviceList = serviceList;
            this.virtueNodes = virtueNodes;
            serviceList.forEach(service ->{
                addServiceToHashCircle(service);
            });
        }

        @Override
        public InetSocketAddress getNext() {
            RpcRequest rpcRequest = HBootstrap.REQUEST_THREAD_LOCAL.get();
            long requestId = rpcRequest.getRequestId();
            int hash = hash(Long.toString(requestId));
            if(hashCircle.containsKey(hash)){
                return hashCircle.get(hash);
            }
            SortedMap<Integer, InetSocketAddress> tailMap = hashCircle.tailMap(hash);
            if(tailMap.isEmpty()){
                hash = hashCircle.firstKey();
            }else {
                hash = tailMap.firstKey();
            }
            return hashCircle.get(hash);
        }

        private void addServiceToHashCircle(InetSocketAddress service) {
            for (int i = 0; i < virtueNodes; i++) {
                int hash = hash(service.toString()+"-"+i);
                hashCircle.put(hash,service);
                if(log.isDebugEnabled()){
                    log.debug("hash为[{}]的节点已经挂载到了哈希环上.",hash);
                }
            }
        }

        private void removeServiceToHashCircle(InetSocketAddress service) {
            for (int i = 0; i < virtueNodes; i++) {
                int hash = hash(service.toString()+"-"+i);
                hashCircle.remove(hash);
                if(log.isDebugEnabled()){
                    log.debug("hash为[{}]的节点已经从哈希环删除.",hash);
                }
            }
        }

        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
            // 取4个字节返回int
            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if( digest[i] < 0 ){
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }
            }
            return res;
        }
    }
}

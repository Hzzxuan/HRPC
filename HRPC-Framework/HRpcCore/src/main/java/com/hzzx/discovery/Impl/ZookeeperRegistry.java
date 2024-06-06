package com.hzzx.discovery.Impl;

import com.hzzx.Constant;
import com.hzzx.HBootstrap;
import com.hzzx.ServiceConfig;
import com.hzzx.discovery.Registry;
import com.hzzx.exceptions.NetworkException;
import com.hzzx.exceptions.RegistryConnectException;
import com.hzzx.utils.NetUtils;
import com.hzzx.utils.ZookeeperNode;
import com.hzzx.utils.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : HuangZx
 * @date : 2024/5/29 14:26
 */
@Slf4j
public class ZookeeperRegistry implements Registry {
    private ZooKeeper zooKeeper;

    public ZookeeperRegistry(){
        zooKeeper = ZookeeperUtils.createZookeeper();
    }
    public ZookeeperRegistry(String host,int timeOut){
        zooKeeper = ZookeeperUtils.createZookeeper(host,timeOut);
    }
    @Override
    public void registry(ServiceConfig<?> service) {
        //创建服务持久节点
        String parentPath = Constant.ZK_PROVIDER_PATH+"/"+ service.getInterface().getName();
        ZookeeperNode node = new ZookeeperNode(parentPath,null);
        if(!ZookeeperUtils.exists(zooKeeper,parentPath,null)){
            ZookeeperUtils.createZookeeperNode(zooKeeper,node,null, CreateMode.PERSISTENT);
        }

        // 创建本机的临时节点, ip:port
        // 服务提供方的端口自己设定
        // ip为一个局域网ip，不是环回地址,也不是ipv6
        //todo: 后续处理端口的问题
        String tempNode = parentPath + "/" + NetUtils.getIp()+":" + HBootstrap.getInstance().getConfiguration().getPort();
        if(!ZookeeperUtils.exists(zooKeeper,tempNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(tempNode,null);
            ZookeeperUtils.createZookeeperNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }
        if(log.isDebugEnabled()){
            log.debug("服务{}，已经被注册",service.getInterface().getName());
        }


    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        String servicePath = Constant.ZK_PROVIDER_PATH+"/"+serviceName;
        if(!ZookeeperUtils.exists(zooKeeper,servicePath,null)){
            log.info("查询不到{}服务",serviceName);
        }
        List<String> children = ZookeeperUtils.getChildren(zooKeeper,servicePath,null);
        List<InetSocketAddress> addresses = children.stream().map(childIpString -> {
            return new InetSocketAddress(childIpString.split(":")[0],
                    Integer.parseInt(childIpString.split(":")[1]));
        }).collect(Collectors.toList());
        if(addresses.isEmpty() || (addresses.size() == 0)){
            throw new NetworkException();
        }
        return addresses;
    }
}

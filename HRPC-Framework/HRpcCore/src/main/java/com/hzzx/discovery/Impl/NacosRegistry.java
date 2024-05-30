package com.hzzx.discovery.Impl;

import com.hzzx.ServiceConfig;
import com.hzzx.discovery.Registry;
import com.hzzx.utils.ZookeeperUtils;

import java.net.InetSocketAddress;

/**
 * @author : HuangZx
 * @date : 2024/5/29 14:26
 */
public class NacosRegistry implements Registry {
    private String host;
    private int timeOut;
    public NacosRegistry(String host,int timeOut){
        this.host = host;
        this.timeOut = timeOut;
    }
    @Override
    public void registry(ServiceConfig<?> service) {

    }

    @Override
    public InetSocketAddress lookup(String serviceName) {
        return null;
    }
}

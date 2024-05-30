package com.hzzx.discovery;

import com.hzzx.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * @author : HuangZx
 * @date : 2024/5/29 14:26
 */
public interface Registry {
    /**
     * 向注册中心注册一个服务
     * @param service
     */
    void registry(ServiceConfig<?> service);

    /**
     * 在注册中心查找一个可用服务节点
     * @param serviceName
     * @return
     */

    InetSocketAddress lookup(String serviceName);
}

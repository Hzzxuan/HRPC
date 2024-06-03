package com.hzzx.loadbalance;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author : HuangZx
 * @date : 2024/6/2 20:55
 */
public interface LoadBalancer {
    InetSocketAddress chooseServiceAddress(String serviceName);

}

package com.hzzx;

import com.hzzx.discovery.RegistryConfig;
import com.hzzx.service.GreetingService;
import com.hzzx.service.impl.GreetingServiceImpl;

import static java.lang.Thread.sleep;

/**
 * @author : HuangZx
 * @date : 2024/5/28 14:44
 */
public class Application {
    public static void main(String[] args) {
        //定义具体的服务
        ServiceConfig<HelloRPC> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterface(HelloRPC.class);
        serviceConfig.setRef(new GreetingServiceImpl());


        //启动服务提供 --配置注册中心、压缩方式
        HBootstrap.getInstance()
                .application("xxx-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocal(new ProtocalConfig("jdk"))
                .publish(serviceConfig)
                .start();

    }

}
package com.hzzx;


import com.hzzx.discovery.RegistryConfig;
import com.hzzx.utils.ZookeeperNode;
import com.hzzx.utils.ZookeeperUtils;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

/**
 * @author : HuangZx
 * @date : 2024/5/28 15:24
 */
public class HBootstrap {
    private static final HBootstrap hBootstrap = new HBootstrap();
    private String applicationName;
    private RegistryConfig registryConfig;
    private ProtocalConfig protocalConfig;

    private static Map<String,ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);


    /**
     * 得到单例对象
     * @return
     */
    public static HBootstrap getInstance() {
        return hBootstrap;
    }

    /**
     * 定义当前应用的Id，将来可能传入注册中心
     * @return
     */
    public HBootstrap application(String name) {
        this.applicationName = name;
        return this;
    }

    /**
     * 连接注册中心
     * @return
     */
    public HBootstrap registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 配置序列化协议
     * @param protocalConfig
     * @return
     */
    public HBootstrap protocal(ProtocalConfig protocalConfig) {
        this.protocalConfig = protocalConfig;
        return this;
    }

    /**
     * 向注册中心注册服务
     * @return
     */
    public HBootstrap publish(ServiceConfig<?> service) {
        //获取注册中心信息并注册服务
        registryConfig.getRegistry().registry(service);
        //维护映射关系 方法名<--->服务
        SERVICE_LIST.put(service.getInterface().getName(),service);
        return this;
    }

    /**
     * 批量向注册中心注册服务
     * @param serviceList
     * @return
     */
    public HBootstrap publish(List<ServiceConfig> serviceList) {
        return this;
    }

    public void start() {
        try {
            sleep(1000000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     ****************************请求端api*****************************
     */

    public void reference(ReferenceConfig<?> reference) {
        reference.setRegistryConfig(registryConfig);
    }
}

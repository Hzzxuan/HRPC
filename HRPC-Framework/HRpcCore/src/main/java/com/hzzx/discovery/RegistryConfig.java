package com.hzzx.discovery;

import com.hzzx.Constant;
import com.hzzx.discovery.Impl.NacosRegistry;
import com.hzzx.discovery.Impl.ZookeeperRegistry;
import com.hzzx.exceptions.RegistryConnectException;

/**
 * @author : HuangZx
 * @date : 2024/5/28 16:09
 */
public class RegistryConfig {
    //类似zookeeper://127.0.0.1:2181
    private String connectString;
    public RegistryConfig(String connectString){
        this.connectString = connectString;
    }

    public Registry getRegistry(){
        String registryType = getRegistryType(connectString);
        // 1、获取注册中心的类型
        // 2、通过类型获取具体注册中心
        if( registryType.equals("zookeeper") ){
            String host = getRegistryHost(connectString);
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        } else if (registryType.equals("nacos")){
            String host = getRegistryHost(connectString);
            return new NacosRegistry(host, Constant.TIME_OUT);
        }
        throw new RegistryConnectException("未发现合适的注册中心。");
    }

    public String getRegistryType(String connectString){
        String[] split = connectString.split("://");
        if(split.length!=2){
            throw new RegistryConnectException("连接信息错误");
        }
        return split[0];
    }

    public String getRegistryHost(String connectString){
        String[] split = connectString.split("://");
        if(split.length!=2){
            throw new RegistryConnectException("连接信息错误");
        }
        return split[1];
    }

}

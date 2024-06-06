package com.hzzx.config;

import com.hzzx.ProtocalConfig;
import com.hzzx.discovery.RegistryConfig;
import com.hzzx.loadbalance.Impl.RoundRobinLoadBalancer;
import com.hzzx.loadbalance.LoadBalancer;
import com.hzzx.utils.IdGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : HuangZx
 * @date : 2024/6/5 23:56
 */
@Data
public class Configuration {
    //端口号
    private int port = 8101;
    //应用程序名
    private String applicationName = "default";
    //注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    // 配置信息-->序列化协议
    private String serializeType = "jdk";

    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";
    //ID生成器
    public IdGenerator ID_GENERATOR= new IdGenerator(0,1);
    //负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer(this.registryConfig.getRegistry());

    public Configuration() {

    }

}

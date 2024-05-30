package com.hzzx;

import com.hzzx.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : HuangZx
 * @date : 2024/5/28 14:42
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        //得到代理对象
        ReferenceConfig<HelloRPC> referenceConfig = new ReferenceConfig();
        referenceConfig.setInterface(HelloRPC.class);

        HBootstrap.getInstance()
                .application("consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(referenceConfig);

        //使用代理对象
        HelloRPC helloRPC = referenceConfig.get();
        String getResult = helloRPC.Hello("hello");
        log.info("getResult--->{}",getResult);
    }
}

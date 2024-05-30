package com.hzzx;

import com.hzzx.discovery.RegistryConfig;

/**
 * @author : HuangZx
 * @date : 2024/5/28 14:42
 */
public class Application {
    public static void main(String[] args) {
        //得到代理对象
        ReferenceConfig<HelloRPC> reference = new ReferenceConfig();
        reference.setInterface(HelloRPC.class);

        HBootstrap.getInstance()
                .application("consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        //使用代理对象
        HelloRPC helloRPC = reference.get();
        helloRPC.Hello("hello");
    }
}

package com.hzzx;

import com.hzzx.discovery.Registry;
import com.hzzx.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author : HuangZx
 * @date : 2024/5/28 21:47
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private RegistryConfig registryConfig;
    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }
    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public T get() {
        //log.info("{}",interfaceRef.getClassLoader());
        //log.info("{}",interfaceRef);
        //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        Object proxy = Proxy.newProxyInstance(interfaceRef.getClassLoader(), classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log.info("method:{}", method.getName());
                log.info("args:{}", args);
                //consumer端需要去注册中心找到需要的服务。传入的是method和args
                Registry registry = registryConfig.getRegistry();
                //查找注册中心，得到可用节点，返回ip+端口
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                if(log.isDebugEnabled()){
                    log.info("服务调用方，发现了服务【{}】可用主机{}",interfaceRef.getName(),address);
                }

                //
                return null;
            }
        });
        return (T) proxy;
    }
}

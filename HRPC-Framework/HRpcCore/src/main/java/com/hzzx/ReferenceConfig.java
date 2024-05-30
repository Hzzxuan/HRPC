package com.hzzx;

import com.hzzx.discovery.Registry;
import com.hzzx.discovery.RegistryConfig;
import com.hzzx.exceptions.NetworkException;
import com.hzzx.proxy.ConsumerInvocationHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
        InvocationHandler invocationHandler = new ConsumerInvocationHandler(registryConfig,interfaceRef);
        Object proxy = Proxy.newProxyInstance(interfaceRef.getClassLoader(), classes, invocationHandler);
        return (T) proxy;
    }
}

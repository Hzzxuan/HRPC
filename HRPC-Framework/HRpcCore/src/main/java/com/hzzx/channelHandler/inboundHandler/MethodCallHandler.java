package com.hzzx.channelHandler.inboundHandler;

import com.hzzx.HBootstrap;
import com.hzzx.ServiceConfig;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author : HuangZx
 * @date : 2024/5/31 23:28
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        //进行方法调用拿到返回值
        RequestLoad requestLoad = rpcRequest.getRequestLoad();
        Object result = methodCall(requestLoad);
        //todo
        //封装响应

        //发送响应
        ctx.channel().writeAndFlush(result);
    }

    private Object methodCall(RequestLoad requestLoad) {
        String interfaceName = requestLoad.getInterfaceName();
        String methodName = requestLoad.getMethodName();
        Class<?>[] parametersType = requestLoad.getParametersType();
        Object[] parametersValue = requestLoad.getParametersValue();
        ServiceConfig<?> serviceConfig = HBootstrap.SERVICE_LIST.get(interfaceName);
        Object ref = serviceConfig.getRef();//具体的实现类实例对象
        Class targetClass = ref.getClass();
        Method method = null;
        try {
            method = targetClass.getMethod(methodName, parametersType);
            Object callResult = method.invoke(ref, parametersValue);
            return callResult;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("调用服务【{}】方法【{}】时发生异常",interfaceName,methodName);
            throw new RuntimeException(e);
        }
    }
}


package com.hzzx.channelHandler.inboundHandler;

import com.hzzx.HBootstrap;
import com.hzzx.ServiceConfig;
import com.hzzx.enumeration.ResponseCode;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcRequest;
import com.hzzx.message.RpcResponse;
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
    private byte code;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        //进行方法调用拿到返回值
        RequestLoad requestLoad = rpcRequest.getRequestLoad();
        Object result = null;
        if(requestLoad==null){
            this.code = ResponseCode.SUCCESS.getCode();
            RpcResponse rpcResponse = encodeResponse(rpcRequest, null);
            ctx.channel().writeAndFlush(rpcResponse);
            return;
        }
        result = methodCall(requestLoad);
        //todo
        //封装响应
        RpcResponse rpcResponse = encodeResponse(rpcRequest,result);

        //发送响应
        ctx.channel().writeAndFlush(rpcResponse);
    }

    private RpcResponse encodeResponse(RpcRequest rpcRequest,Object callResult) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        rpcResponse.setCompressType(rpcRequest.getCompressType());
        rpcResponse.setSerializeType(rpcRequest.getSerializeType());
        rpcResponse.setCallResult(callResult);
        rpcResponse.setCode(code);
        return rpcResponse;
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
            this.code = ResponseCode.SUCCESS.getCode();
            return callResult;
        } catch (NoSuchMethodException | InvocationTargetException e) {
            this.code = ResponseCode.FAIL.getCode();
            log.error("调用服务【{}】方法【{}】时发生异常",interfaceName,methodName);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            this.code = ResponseCode.ILLEGALACCESS.getCode();
            log.error("调用服务【{}】方法【{}】时发生异常",interfaceName,methodName);
            throw new RuntimeException(e);
        }
    }
}



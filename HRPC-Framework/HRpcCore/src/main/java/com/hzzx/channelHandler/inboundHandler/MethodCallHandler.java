package com.hzzx.channelHandler.inboundHandler;

import com.hzzx.HBootstrap;
import com.hzzx.ServiceConfig;
import com.hzzx.enumeration.RequestType;
import com.hzzx.enumeration.ResponseCode;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcRequest;
import com.hzzx.message.RpcResponse;
import com.hzzx.protection.CircuitBreaker;
import com.hzzx.protection.RateLimiter;
import com.hzzx.protection.TokenBucketRateLimiter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @author : HuangZx
 * @date : 2024/5/31 23:28
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private byte code;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {

        Channel channel = ctx.channel();
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = HBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();
        if(!everyIpRateLimiter.containsKey(socketAddress)){
            everyIpRateLimiter.put(socketAddress,new TokenBucketRateLimiter(10,10));
        }
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        Object result = null;
        //封装部分响应
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        rpcResponse.setCompressType(rpcRequest.getCompressType());
        rpcResponse.setSerializeType(rpcRequest.getSerializeType());
        if(!rateLimiter.allowRequest()){
            rpcResponse.setCode(ResponseCode.RATE_LIMIT.getCode());
        }else if(rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()){
            rpcResponse.setCode(ResponseCode.SUCCESS_HEART_BEAT.getCode());
        }else{
            try{
                //进行方法调用拿到返回值
                RequestLoad requestLoad = rpcRequest.getRequestLoad();
                rpcResponse.setCode(ResponseCode.SUCCESS.getCode());
                if(requestLoad != null){
                    result = methodCall(requestLoad);
                }
                rpcResponse.setCallResult(result);
            }catch (Exception e){
                log.error("调用函数过程中发生异常");
                rpcResponse.setCode(ResponseCode.FAIL.getCode());
            }

        }
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



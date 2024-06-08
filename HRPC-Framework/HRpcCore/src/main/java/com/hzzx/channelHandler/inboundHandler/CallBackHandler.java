package com.hzzx.channelHandler.inboundHandler;

import com.hzzx.HBootstrap;
import com.hzzx.enumeration.ResponseCode;
import com.hzzx.exceptions.ResponseException;
import com.hzzx.message.RpcResponse;
import com.hzzx.protection.CircuitBreaker;
import com.hzzx.proxy.ConsumerInvocationHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author : HuangZx
 * @date : 2024/6/1 16:37
 */
@Slf4j
public class CallBackHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        byte code = rpcResponse.getCode();
        CompletableFuture<Object> completableFuture = HBootstrap.PENDING_FUTURE.get(rpcResponse.getRequestId());
        CircuitBreaker circuitBreaker = ConsumerInvocationHandler.circuitBreaker;
        if(code == ResponseCode.FAIL.getCode()){
            circuitBreaker.incrFailCount();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，返回错误的结果，响应码[{}].",
                    rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.FAIL.getDescription());

        } else if (code == ResponseCode.RATE_LIMIT.getCode()){
            circuitBreaker.incrFailCount();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，被限流，响应码[{}].",
                    rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.RATE_LIMIT.getDescription());

        } else if (code == ResponseCode.RESOURCE_NOT_FOUND.getCode() ){
            circuitBreaker.incrFailCount();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，未找到目标资源，响应码[{}].",
                    rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.RESOURCE_NOT_FOUND.getDescription());

        } else if (code == ResponseCode.SUCCESS.getCode() ){
            // 服务提供方，给予的结果
            Object returnValue = rpcResponse.getCallResult();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture，处理响应结果。", rpcResponse.getRequestId());
            }
        } else if(code == ResponseCode.SUCCESS_HEART_BEAT.getCode()){
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture,处理心跳检测，处理响应结果。", rpcResponse.getRequestId());
            }
        }
    }
}

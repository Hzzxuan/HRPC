package com.hzzx.channelHandler.inboundHandler;

import com.hzzx.HBootstrap;
import com.hzzx.enumeration.ResponseCode;
import com.hzzx.message.RpcResponse;
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
        if(code != ResponseCode.SUCCESS.getCode()){
            log.error("获取响应失败");
            //对HbootStrap中挂起的事务进行回复
            CompletableFuture<Object> completableFuture = HBootstrap.PENDING_FUTURE.get(rpcResponse.getRequestId());
            completableFuture.complete(null);
        }
        CompletableFuture<Object> completableFuture = HBootstrap.PENDING_FUTURE.get(rpcResponse.getRequestId());
        completableFuture.complete(rpcResponse.getCallResult());
    }
}

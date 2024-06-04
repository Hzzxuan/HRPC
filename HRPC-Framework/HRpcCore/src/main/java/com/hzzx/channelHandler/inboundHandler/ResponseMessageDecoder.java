package com.hzzx.channelHandler.inboundHandler;

import com.hzzx.channelHandler.MessageConstant;
import com.hzzx.compress.CompressFactory;
import com.hzzx.compress.Compressor;
import com.hzzx.enumeration.RequestType;
import com.hzzx.enumeration.ResponseCode;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcRequest;
import com.hzzx.message.RpcResponse;
import com.hzzx.serialize.Serializer;
import com.hzzx.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.compression.CompressionException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author : HuangZx
 * @date : 2024/6/1 16:27
 */
@Slf4j
public class ResponseMessageDecoder extends LengthFieldBasedFrameDecoder {
    public ResponseMessageDecoder() {
        super(  //最大帧长度
                MessageConstant.MAX_FRAME_LENGTH,
                //报文总长度的index
                MessageConstant.MAGIC.length+MessageConstant.VERSION_LENGTH+MessageConstant.HEAD_LEN_LENGTH,
                //长度所占位宽
                MessageConstant.FULL_FIELD_LENGTH,
                //负载的适应长度,这里fullLenth后跟的就是具体报文，因此把其前面的都减掉，具体见LengthFieldBasedFrameDecoder文档
                -(MessageConstant.MAGIC.length+MessageConstant.VERSION_LENGTH
                        +MessageConstant.HEAD_LEN_LENGTH+MessageConstant.FULL_FIELD_LENGTH)
                , 0);

    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if(decode instanceof ByteBuf){
            ByteBuf byteBuf = (ByteBuf) decode;
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        byte[] magic = new byte[MessageConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if(magic[i]!=MessageConstant.MAGIC[i]){
                throw new RuntimeException("获取的响应不合法");
            }
        }
        byte version = byteBuf.readByte();
        if(version!=MessageConstant.VERSION){
            throw new RuntimeException("响应的版本号不匹配 ");
        }
        //报文头长度
        short headLength = byteBuf.readShort();
        //总长度
        int fullLength = byteBuf.readInt();
        //请求类型
        byte code = byteBuf.readByte();
        //序列化类型
        byte serializeType = byteBuf.readByte();
        //压缩类型
        byte compressType = byteBuf.readByte();
        //请求Id
        long requestId = byteBuf.readLong();

        //if(code != ResponseCode.SUCCESS.getCode()){
        //    return null;
        //}

        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(code);
        rpcResponse.setSerializeType(serializeType);
        rpcResponse.setCompressType(compressType);
        rpcResponse.setRequestId(requestId);

        //得到报文体，进行报文体的解压缩、反序列化
        int callResultLength = fullLength - headLength;
        if (callResultLength == 0) {
            return rpcResponse;
        }
        byte[] callResultBytes = new byte[callResultLength];
        byteBuf.readBytes(callResultBytes);
        Compressor compressor = CompressFactory.getCompressor(compressType);
        callResultBytes = compressor.deCompress(callResultBytes);
        Serializer serializer = SerializerFactory.getSerializer(serializeType);
        Object callResult = serializer.deSerialize(callResultBytes);
        rpcResponse.setCallResult(callResult);

        /*
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(callResultBytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);)
        {
            Object callResult = objectInputStream.readObject();
            rpcResponse.setCallResult(callResult);
        } catch (IOException | ClassNotFoundException e) {
            log.error("响应【{}】序列化时发生错误",requestId,e);
        }*/
        return rpcResponse;

    }
}

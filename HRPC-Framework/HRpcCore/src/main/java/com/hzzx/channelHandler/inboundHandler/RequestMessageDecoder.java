package com.hzzx.channelHandler.inboundHandler;

import com.hzzx.channelHandler.MessageConstant;
import com.hzzx.compress.CompressFactory;
import com.hzzx.compress.Compressor;
import com.hzzx.enumeration.RequestType;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcRequest;
import com.hzzx.serialize.Impl.JdkSerializer;
import com.hzzx.serialize.Serializer;
import com.hzzx.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author : HuangZx
 * @date : 2024/5/31 14:58
 */

/**
 * 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 * +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 * |                                                                                                             |
 * |                                         body                                                                |
 * |                                                                                                             |
 * +--------------------------------------------------------------------------------------------------------+-----+
 * 报文格式如上所示
 */
@Slf4j
public class RequestMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RequestMessageDecoder() {
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
                throw new RuntimeException("获取的请求不合法");
            }
        }
        byte version = byteBuf.readByte();
        if(version!=MessageConstant.VERSION){
            throw new RuntimeException("请求的版本号不匹配 ");
        }
        //报文头长度
        short headLength = byteBuf.readShort();
        //总长度
        int fullLength = byteBuf.readInt();
        //请求类型
        byte requestType = byteBuf.readByte();
        //序列化类型
        byte serializeType = byteBuf.readByte();
        //压缩类型
        byte compressType = byteBuf.readByte();
        //请求Id
        long requestId = byteBuf.readLong();

        if(requestType == RequestType.HEART_BEAT.getId()){
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(requestType);
        rpcRequest.setSerializeType(serializeType);
        rpcRequest.setCompressType(compressType);
        rpcRequest.setRequestId(requestId);
        //报文体
        int RpcLoadLength = fullLength - headLength;
        byte[] RpcLoad = new byte[RpcLoadLength];
        byteBuf.readBytes(RpcLoad);
        //得到报文体，进行报文体的解压缩和反序列化
        Compressor compressor = CompressFactory.getCompressor(compressType);
        RpcLoad = compressor.deCompress(RpcLoad);
        Serializer serializer = SerializerFactory.getSerializer(serializeType);
        Object o = serializer.deSerialize(RpcLoad);
        rpcRequest.setRequestLoad((RequestLoad) o);
        /*
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(RpcLoad);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);){
            RequestLoad requestLoad = (RequestLoad) objectInputStream.readObject();
            rpcRequest.setRequestLoad(requestLoad);
        } catch (IOException | ClassNotFoundException e) {
            log.error("请求【{}】序列化时发生错误",requestId,e);
        }

         */
        return rpcRequest;

    }


}

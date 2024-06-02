package com.hzzx.channelHandler.outboundHandler;

import com.hzzx.channelHandler.MessageConstant;
import com.hzzx.enumeration.RequestType;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcRequest;
import com.hzzx.serialize.Serializer;
import com.hzzx.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author : HuangZx
 * @date : 2024/5/31 11:15
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
 *
 */

/**
 * 4B magic
 * 1B version
 * 2B head Len
 * 4B full Len
 * 1B qt
 * 1B serialize
 * 1B compress
 * 8B requestId
 */
@Slf4j
public class RequestMessageEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(MessageConstant.MAGIC);
        byteBuf.writeByte(MessageConstant.VERSION);
        byteBuf.writeShort(MessageConstant.HEAD_LEN);
        byteBuf.writerIndex(byteBuf.writerIndex()+MessageConstant.FULL_FIELD_LENGTH);
        //byteBuf.writeInt(MessageConstant.FULL_LEN);
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializeType());
        byteBuf.writeByte(rpcRequest.getCompressType());
        byteBuf.writeLong(rpcRequest.getRequestId());

        //如果是心跳请求，不添加请求体，处理总长度
        if(rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()){
            int index = byteBuf.writerIndex();
            byteBuf.writerIndex(MessageConstant.MAGIC.length+MessageConstant.VERSION_LENGTH+MessageConstant.HEAD_LEN_LENGTH);
            byteBuf.writeInt(MessageConstant.HEAD_LEN);
            byteBuf.writerIndex(index);
            return;
        }

        /**--------------------------进行报文序列化-----------------------**/
        RequestLoad requestLoad = rpcRequest.getRequestLoad();
        Serializer serializer = SerializerFactory.getSerializer(rpcRequest.getSerializeType());
        byte[] bodyBytes = serializer.serialize(requestLoad);
        /**-------------------------进行报文压缩-------------------------**/

        byteBuf.writeBytes(bodyBytes);
        int index = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageConstant.MAGIC.length+MessageConstant.VERSION_LENGTH+MessageConstant.HEAD_LEN_LENGTH);
        byteBuf.writeInt(MessageConstant.HEAD_LEN + bodyBytes.length);
        byteBuf.writerIndex(index);
        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已经完成报文的编码。", rpcRequest.getRequestId());
        }
    }

    /*
    private byte[] getBodyBytes(RequestLoad requestLoad) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(requestLoad);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/
}

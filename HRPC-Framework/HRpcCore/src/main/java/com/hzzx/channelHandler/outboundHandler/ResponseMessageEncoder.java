package com.hzzx.channelHandler.outboundHandler;

import com.hzzx.channelHandler.MessageConstant;
import com.hzzx.enumeration.RequestType;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcResponse;
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
 * @date : 2024/6/1 15:02
 */
/**
 * 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * |    magic          |ver |head  len|    full length    | cd | ser|comp|              RequestId                |
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
 * 1B code
 * 1B serialize
 * 1B compress
 * 8B requestId
 */
@Slf4j
public class ResponseMessageEncoder extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(MessageConstant.MAGIC);
        byteBuf.writeByte(MessageConstant.VERSION);
        byteBuf.writeShort(MessageConstant.HEAD_LEN);
        byteBuf.writerIndex(byteBuf.writerIndex()+MessageConstant.FULL_FIELD_LENGTH);
        //byteBuf.writeInt(MessageConstant.FULL_LEN);
        byteBuf.writeByte(rpcResponse.getCode());
        byteBuf.writeByte(rpcResponse.getSerializeType());
        byteBuf.writeByte(rpcResponse.getCompressType());
        byteBuf.writeLong(rpcResponse.getRequestId());
        Object callResult = rpcResponse.getCallResult();
        /**---------------------响应结果序列化-----------------**/
        Serializer serializer = SerializerFactory.getSerializer(rpcResponse.getSerializeType());
        byte[] callResultBytes = serializer.serialize(callResult);
        if(callResultBytes != null){
            byteBuf.writeBytes(callResultBytes);
        }
        int callResultBytesLength = callResultBytes == null ? 0 : callResultBytes.length;
        int index = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageConstant.MAGIC.length+MessageConstant.VERSION_LENGTH+MessageConstant.HEAD_LEN_LENGTH);
        byteBuf.writeInt(MessageConstant.HEAD_LEN + callResultBytesLength);
        byteBuf.writerIndex(index);
        if (log.isDebugEnabled()) {
            log.debug("响应【{}】已经完成报文的编码。", rpcResponse.getRequestId());
        }

    }

    private byte[] getBodyBytes(Object object) {
        if(object == null){
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

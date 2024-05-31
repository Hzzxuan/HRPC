package com.hzzx.channelHandler.outboundHandler;

import com.hzzx.channelHandler.MessageConstant;
import com.hzzx.message.RequestLoad;
import com.hzzx.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

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
public class MessageEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(MessageConstant.MAGIC);
        byteBuf.writeByte(MessageConstant.VERSION);
        byteBuf.writeShort(MessageConstant.HEAD_LEN);
        byteBuf.writerIndex(byteBuf.writerIndex()+4);
        //byteBuf.writeInt(MessageConstant.FULL_LEN);
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializeType());
        byteBuf.writeByte(rpcRequest.getCompressType());
        byteBuf.writeLong(rpcRequest.getTimeStamp());
        byte[] bodyBytes = getBodyBytes(rpcRequest.getRequestLoad());
        byteBuf.writeBytes(bodyBytes);
        int index = byteBuf.writerIndex();
        byteBuf.writerIndex(7);
        byteBuf.writeLong(MessageConstant.HEAD_LEN + bodyBytes.length);
        byteBuf.writerIndex(index);

    }

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
    }
}

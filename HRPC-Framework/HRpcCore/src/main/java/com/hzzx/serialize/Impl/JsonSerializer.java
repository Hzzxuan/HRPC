package com.hzzx.serialize.Impl;

import com.alibaba.fastjson2.JSONB;
import com.hzzx.serialize.Serializer;

import java.io.ByteArrayOutputStream;


/**
 * @author : HuangZx
 * @date : 2024/6/2 13:03
 */
/**-----------------json的序列化不能这样用json没法接收.class字节码对象，需要特殊处理------------------**/
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if(object == null){
            return null;
        }
        byte[] bytes = JSONB.toBytes(object);
        return bytes;
    }

    @Override
    public Object deSerialize(byte[] bytes) {
        if(bytes == null){
            return null;
        }
        Object parse = JSONB.parse(bytes);
        return parse;
    }
}

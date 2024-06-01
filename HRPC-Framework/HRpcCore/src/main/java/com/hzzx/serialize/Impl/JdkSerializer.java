package com.hzzx.serialize.Impl;

import com.hzzx.serialize.Serializer;

/**
 * @author : HuangZx
 * @date : 2024/6/1 23:42
 */
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public Object deSerialize(byte[] bytes) {
        return null;
    }
}

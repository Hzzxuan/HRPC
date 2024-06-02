package com.hzzx.serialize.Impl;

import com.hzzx.serialize.Serializer;

/**
 * @author : HuangZx
 * @date : 2024/6/2 13:03
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public Object deSerialize(byte[] bytes) {
        return null;
    }
}

package com.hzzx.serialize;

/**
 * @author : HuangZx
 * @date : 2024/6/1 23:40
 */
public interface Serializer {

    byte[] serialize(Object object);

    Object deSerialize(byte[] bytes);

}

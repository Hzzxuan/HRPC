package com.hzzx.serialize.Impl;

import com.hzzx.message.RequestLoad;
import com.hzzx.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author : HuangZx
 * @date : 2024/6/1 23:42
 */
@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("请求序列化时发生错误",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deSerialize(byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);){
            Object object = objectInputStream.readObject();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("请求序列化时发生错误",e);
            throw new RuntimeException(e);
        }
    }
}

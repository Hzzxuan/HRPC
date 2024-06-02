package com.hzzx.serialize;

import com.hzzx.serialize.Impl.JdkSerializer;
import com.hzzx.serialize.Impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : HuangZx
 * @date : 2024/6/1 23:44
 */
@Slf4j
//通过序列化工程拿到序列化器
public class SerializerFactory {
    private static final ConcurrentHashMap<String,ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private static final ConcurrentHashMap<Byte,Serializer> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Serializer> JDK_SERIALIZER = new ObjectWrapper<Serializer>((byte) 1, new JdkSerializer());
        ObjectWrapper<Serializer> JSON_SERIALIZER = new ObjectWrapper<Serializer>((byte) 2, new JsonSerializer());
        SERIALIZER_CACHE.put("jdk",JDK_SERIALIZER);
        SERIALIZER_CACHE.put("json",JSON_SERIALIZER);
        SERIALIZER_CACHE_CODE.put(JDK_SERIALIZER.getCode(),JDK_SERIALIZER.getObject());
        SERIALIZER_CACHE_CODE.put(JSON_SERIALIZER.getCode(),JSON_SERIALIZER.getObject());
    }
    public static ObjectWrapper<Serializer> getSerializerWrapper(String name){
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(name);
        if(serializerWrapper == null){
            log.error("未找到您配置的【{}】序列化工具，默认选用jdk的序列化方式。",name);
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

    public static Serializer getSerializer(byte code){
        Serializer serializer = SERIALIZER_CACHE_CODE.get(code);
        if(serializer == null){
            log.error("序列化器不存在，采用默认jdk序列化器");
            return SERIALIZER_CACHE_CODE.get("jdk");
        }
        return serializer;
    }
}

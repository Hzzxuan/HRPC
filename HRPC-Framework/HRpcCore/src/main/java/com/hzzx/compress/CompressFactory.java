package com.hzzx.compress;

import com.hzzx.ObjectWrapper;
import com.hzzx.compress.Impl.GZipCompressor;
import com.hzzx.serialize.Impl.HessianSerializer;
import com.hzzx.serialize.Impl.JdkSerializer;
import com.hzzx.serialize.Impl.JsonSerializer;
import com.hzzx.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : HuangZx
 * @date : 2024/6/1 23:44
 */
@Slf4j
//通过序列化工程拿到序列化器
public class CompressFactory {
    private static final ConcurrentHashMap<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private static final ConcurrentHashMap<Byte,Compressor> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Compressor> ZIP_COMPRESSOR = new ObjectWrapper<Compressor>((byte) 1, new GZipCompressor());

        COMPRESSOR_CACHE.put("gzip",ZIP_COMPRESSOR);
        COMPRESSOR_CACHE_CODE.put(ZIP_COMPRESSOR.getCode(),ZIP_COMPRESSOR.getObject());
    }
    public static ObjectWrapper<Compressor> getCompressorWrapper(String name){
        ObjectWrapper<Compressor> compressorWrapper = COMPRESSOR_CACHE.get(name);
        if(compressorWrapper == null){
            log.error("未找到您配置的【{}】压缩工具，默认选用zip的序列化方式。",name);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorWrapper;
    }

    public static Compressor getCompressor(byte code){
        Compressor compressor = COMPRESSOR_CACHE_CODE.get(code);
        if(compressor == null){
            log.error("压缩工具不存在，采用默认zip序列化器");
            return COMPRESSOR_CACHE_CODE.get((byte) 1);
        }
        return compressor;
    }
}

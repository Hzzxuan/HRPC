package com.hzzx.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : HuangZx
 * @date : 2024/5/30 22:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRequest {
    /*-------------------报文头-----------------*/
    //请求Id
    private long requestId;
    //请求类型
    private byte requestType;
    //压缩类型
    private byte compressType;
    //序列化类型
    private byte serializeType;
    //时间戳
    //private long timeStamp;
    /*-------------------报文体-----------------*/
    private RequestLoad requestLoad;

}

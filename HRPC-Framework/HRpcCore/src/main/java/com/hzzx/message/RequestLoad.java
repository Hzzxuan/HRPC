package com.hzzx.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author : HuangZx
 * @date : 2024/5/30 22:42
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestLoad implements Serializable {
    //接口全类名
    private String interfaceName;
    //请求方法名
    private String methodName;
    //参数列表
    private Class<?>[] parametersType;
    private Object[] parametersValue;
    //返回值类型
    private Class<?> returnType;
}

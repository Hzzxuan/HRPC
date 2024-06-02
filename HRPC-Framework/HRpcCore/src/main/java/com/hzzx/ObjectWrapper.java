package com.hzzx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : HuangZx
 * @date : 2024/6/2 12:56
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
//定义为泛型，后续的压缩也要沿用
public class ObjectWrapper<T> {
    private byte code;
    private T object;

}

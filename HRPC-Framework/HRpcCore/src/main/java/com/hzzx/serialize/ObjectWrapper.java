package com.hzzx.serialize;

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
public class ObjectWrapper<T> {
    private byte code;
    private T object;

}

package com.hzzx.protection;

import lombok.Data;

/**
 * @author : HuangZx
 * @date : 2024/6/7 14:15
 */
@Data
public class CircuitBreakerConfig {
    //closed状态的失败次数阈值
    private int failThreshold = 5;

    //closed状态的失败计数的时间窗口
    private int failCountWindowInMs = 60*1000;

    //处于open状态下进入half-open的超时时间
    private int open2HalfOpenTimeoutInMs = 5*1000;

    //half-open状态下成功次数阈值
    private int consecutiveSuccThreshold = 5;

    private CircuitBreakerConfig(){

    }

}

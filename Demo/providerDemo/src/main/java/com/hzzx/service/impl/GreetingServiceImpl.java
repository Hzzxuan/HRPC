package com.hzzx.service.impl;

import com.hzzx.annotation.HRpcSupply;
import com.hzzx.service.GreetingService;

/**
 * @author : HuangZx
 * @date : 2024/5/28 22:05
 */
@HRpcSupply
public class GreetingServiceImpl implements GreetingService {
    @Override
    public String Hello(String s) {
        return "hi consumer:" + s;
    }
}

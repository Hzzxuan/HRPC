package com.hzzx.impl;

/**
 * @author : HuangZx
 * @date : 2024/5/28 22:05
 */
public class HelloRPC implements com.hzzx.HelloRPC {
    @Override
    public String Hello(String s) {
        return "hi consumer:" + s;
    }
}

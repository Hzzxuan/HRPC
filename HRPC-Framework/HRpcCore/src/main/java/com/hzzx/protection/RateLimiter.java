package com.hzzx.protection;

/**
 * @author : HuangZx
 * @date : 2024/6/6 23:44
 */
public interface RateLimiter {
    boolean allowRequest();
}

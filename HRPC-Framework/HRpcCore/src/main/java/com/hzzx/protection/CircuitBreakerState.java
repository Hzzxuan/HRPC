package com.hzzx.protection;

/**
 * @author : HuangZx
 * @date : 2024/6/7 13:40
 */
public enum CircuitBreakerState {
    CLOSED,    // working normally, calls are transparently passing through
    OPEN,      // method calls are being intercepted and CircuitBreakerExceptions are being thrown instead
    HALF_OPEN  // method calls are passing through; if another blacklisted exception is thrown, reverts back to OPEN
}
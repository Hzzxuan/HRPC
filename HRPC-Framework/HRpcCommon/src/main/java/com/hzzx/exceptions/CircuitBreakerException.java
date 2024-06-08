package com.hzzx.exceptions;

/**
 * @author : HuangZx
 * @date : 2024/6/8 11:41
 */
public class CircuitBreakerException extends RuntimeException{
    public CircuitBreakerException() {
    }

    public CircuitBreakerException(String message) {
        super(message);
    }

    public CircuitBreakerException(Throwable cause) {
        super(cause);
    }
}

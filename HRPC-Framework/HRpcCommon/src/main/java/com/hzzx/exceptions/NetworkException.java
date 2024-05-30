package com.hzzx.exceptions;

/**
 * @author : HuangZx
 * @date : 2024/5/29 16:00
 */
public class NetworkException extends RuntimeException{

    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
}

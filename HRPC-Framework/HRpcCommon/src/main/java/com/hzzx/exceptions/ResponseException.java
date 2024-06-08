package com.hzzx.exceptions;

/**
 * @author : HuangZx
 * @date : 2024/6/8 20:36
 */
public class ResponseException extends RuntimeException{
    public ResponseException() {
    }

    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(Throwable cause) {
        super(cause);
    }

    public ResponseException(byte code, String description) {
    }
}

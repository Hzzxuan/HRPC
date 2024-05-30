package com.hzzx.exceptions;

/**
 * @author : HuangZx
 * @date : 2024/5/29 11:07
 */
public class ZookeeperException extends RuntimeException{
    public ZookeeperException(){

    }
    public ZookeeperException(Throwable cause){
        super(cause);
    }
}

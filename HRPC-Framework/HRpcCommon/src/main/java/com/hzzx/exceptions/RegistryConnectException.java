package com.hzzx.exceptions;

/**
 * @author : HuangZx
 * @date : 2024/5/29 14:12
 */
public class RegistryConnectException extends RuntimeException{
    public RegistryConnectException(){
        super();
    }
    public RegistryConnectException(String msg){
        super(msg);
    }

}

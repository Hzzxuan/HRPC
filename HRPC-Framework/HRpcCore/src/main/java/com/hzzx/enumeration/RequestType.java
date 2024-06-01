package com.hzzx.enumeration;

/**
 * @author : HuangZx
 * @date : 2024/6/1 0:01
 */
public enum RequestType {
    REQUEST((byte)1,"normalRequest"), HEART_BEAT((byte)2,"keepAliveRequest");

    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}

package com.hzzx.enumeration;

/**
 * @author : HuangZx
 * @date : 2024/6/1 15:16
 */
public enum ResponseCode {
    SUCCESS((byte)20,"成功"),
    FAIL((byte)50,"调用方法发生异常"),
    ILLEGALACCESS((byte)51,"调用方法发生异常"),

    ;

    private byte code;
    private String description;
    ResponseCode(byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

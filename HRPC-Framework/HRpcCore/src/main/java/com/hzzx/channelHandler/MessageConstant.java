package com.hzzx.channelHandler;

/**
 * @author : HuangZx
 * @date : 2024/5/31 11:39
 */
public class MessageConstant {
    public static final byte[] MAGIC = "HRPC".getBytes();
    public static final byte VERSION = (byte) 1;

    public static final short HEAD_LEN = (byte)(MAGIC.length + 1 + 2 +4 + 1 + 1 + 1 + 8);
    public static final int MAX_FRAME_LENGTH = 1024*1024;
    public static final int MAGIC_LENGTH = 4;
    public static final int VERSION_LENGTH = 1;
    public static final int HEAD_LEN_LENGTH = Short.SIZE/8;
    public static final int FULL_FIELD_LENGTH = Integer.SIZE/8;

}

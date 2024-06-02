package com.hzzx.compress;

/**
 * @author : HuangZx
 * @date : 2024/6/2 17:02
 */
public interface Compressor {
    byte[] compress(byte[] bytes);
    byte[] deCompress(byte[] bytes);
}

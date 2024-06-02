package com.hzzx.compress.Impl;

import com.hzzx.compress.Compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author : HuangZx
 * @date : 2024/6/2 17:02
 */
public class GZipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            ){
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return byteArray;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] deCompress(byte[] bytes) {
        try(ByteArrayInputStream byteArrayinputStream = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayinputStream);
        ){
            byte[] result = gzipInputStream.readAllBytes();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

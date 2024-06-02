package com.hzzx.serialize.Impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.hzzx.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * @author : HuangZx
 * @date : 2024/6/2 15:29
 */
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        byte[] result = null;
        if(object == null){
            return result;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        try {
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            hessian2Output.completeMessage();
            hessian2Output.close();
            result = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;

    }

    @Override
    public Object deSerialize(byte[] bytes) {
        if(bytes == null){
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Hessian2Input input = new Hessian2Input(byteArrayInputStream);
        Object result = null;
        try {
            result = input.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

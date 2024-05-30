package com.hzzx;

/**
 * @author : HuangZx
 * @date : 2024/5/28 21:22
 */
public class ServiceConfig<T> {
    private Class<?> serviceProvider;
    private Object ref;
    public ServiceConfig(){

    }

    public Class<?> getInterface(){
        return serviceProvider;
    }
    public void setInterface(Class<?> serviceProvider){
        this.serviceProvider = serviceProvider;
    }
    public Object getRef() {
        return ref;
    }
    public void setRef(Object ref) {
        this.ref = ref;
    }
}

package com.hzzx;

import com.hzzx.discovery.RegistryConfig;
import com.hzzx.heatBeat.HeartBeatDetect;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author : HuangZx
 * @date : 2024/5/28 14:42
 */
@Slf4j
public class consumerApplication {
    public static void main(String[] args) {
        //得到代理对象
        ReferenceConfig<HelloRPC> referenceConfig = new ReferenceConfig();
        referenceConfig.setInterface(HelloRPC.class);

        HBootstrap.getInstance()
                .application("consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(referenceConfig);

        //使用代理对象
        HelloRPC helloRPC = referenceConfig.get();
        String getResult = "";
        for (int i = 0; i < 20; i++) {
            getResult = helloRPC.Hello("hello");
            log.info("getResult--->{}",getResult);
        }

        Timer timer = new Timer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String x= "";
                        for (int i = 0; i < 5; i++) {
                            x=helloRPC.Hello("hello111");
                            log.info("getResult--->{}",x);
                        }
                    }
                },3000);
            }
        }).start();


        //HeartBeatDetect.start(HelloRPC.class.getName());



    }
}

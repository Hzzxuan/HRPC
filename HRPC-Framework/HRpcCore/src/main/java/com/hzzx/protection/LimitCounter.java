package com.hzzx.protection;

/**
 * @author : HuangZx
 * @date : 2024/6/7 13:53
 */

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用来记录closed状态下失败次数
 */
public class LimitCounter {
    private long startTime;
    private long timeIntervalInMs;
    private int maxLimit;
    private AtomicInteger currentCount;

    public LimitCounter(long timeIntervalInMs, int maxLimit) {
        this.timeIntervalInMs = timeIntervalInMs;
        this.maxLimit = maxLimit;
        startTime = System.currentTimeMillis();
        currentCount = new AtomicInteger(0);
    }

    public int incrAndGet(){
        //判断是否超过时间窗口，超过重置窗口
        long currentTime = System.currentTimeMillis();
        if((startTime+timeIntervalInMs) < currentTime){
            synchronized (this){
                if((startTime+timeIntervalInMs) < currentTime){
                    startTime = currentTime;
                    currentCount.set(0);
                }
            }
        }
        return currentCount.incrementAndGet();
    }

    public boolean thresholdReached(){
        return currentCount.get() > maxLimit;
    }

    public int get(){
        return currentCount.get();
    }

    public synchronized void reset(){
        currentCount.set(0);
    }
}

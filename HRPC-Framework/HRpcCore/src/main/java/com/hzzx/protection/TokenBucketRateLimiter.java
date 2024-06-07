package com.hzzx.protection;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : HuangZx
 * @date : 2024/6/6 23:40
 */
public class TokenBucketRateLimiter implements RateLimiter{
    private int tokens;

    private final int capacity;

    private final int rate;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Timer timer= new Timer();

    private final TimerTask addToken = new TimerTask() {
        @Override
        public void run() {
            //更新令牌数
            tokens += rate;
            if(tokens>capacity){
                tokens = capacity;
            }
        }
    };
    // 上一次放令牌的时间

    public TokenBucketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        tokens = capacity;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                timer.scheduleAtFixedRate(addToken,0L,1000);
            }
        });
    }

    @Override
    public synchronized boolean allowRequest() {
        if(tokens > 0){
            tokens --;
            System.out.println("请求被放行---------------");
            return true;
        } else {
            System.out.println("请求被拦截---------------");
            return false;
        }
    }

    public static void main(String[] args) {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(10,10);
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean allowRequest = rateLimiter.allowRequest();
            System.out.println("allowRequest = " + allowRequest);
        }
    }
}
